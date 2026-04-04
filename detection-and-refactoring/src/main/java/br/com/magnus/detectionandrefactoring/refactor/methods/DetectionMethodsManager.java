package br.com.magnus.detectionandrefactoring.refactor.methods;

import br.com.magnus.config.starter.configuration.JavaParserSingleton;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.projects.Project;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

/**
 * Manages detection and refactoring of design patterns in Java projects.
 * <p>
 * Implementations detect specific design patterns and apply automated
 * refactoring transformations to improve code structure.
 */
public interface DetectionMethodsManager {

    /**
     * Refactors the project by detecting and transforming applicable patterns.
     *
     * @param project the project to refactor
     * @return list of refactored file groups, empty if no refactoring was applied
     */
    List<RefactorFiles> refactor(Project project);

    /**
     * Checks if the candidates list is empty.
     *
     * @param candidates the candidates to check
     * @return true if no candidates were found
     */
    default boolean hasNoCandidates(List<RefactoringCandidate> candidates) {
        return candidates.isEmpty();
    }

    /**
     * Executes tasks in parallel with bounded concurrency.
     * <p>
     * All tasks are submitted immediately, but a semaphore ensures at most {@code parallelism}
     * tasks execute concurrently. This provides true parallelism control unlike batch-based approaches.
     * <p>
     * Each task runs within a scoped JavaParser context to ensure thread safety.
     *
     * @param items      the items to process
     * @param executor   the executor for running tasks
     * @param parallelism the maximum number of concurrent tasks
     * @param task       the function to apply to each item
     * @param <T>        the input type
     * @param <R>        the result type
     * @return list of results in the same order as input items
     * @throws IllegalStateException if parallel execution is interrupted or fails
     */
    static <T, R> List<R> executeInParallel(List<T> items, Executor executor, int parallelism, Function<T, R> task) {
        if (items.isEmpty()) {
            return List.of();
        }

        var concurrencyLimit = Math.max(1, parallelism);
        var semaphore = new Semaphore(concurrencyLimit);

        try {
            var futures = items.stream()
                    .map(item -> CompletableFuture.supplyAsync(() -> {
                        acquirePermit(semaphore);
                        try {
                            return JavaParserSingleton.callWithScopedParser(() -> task.apply(item));
                        } finally {
                            semaphore.release();
                        }
                    }, executor))
                    .toList();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
        } catch (CompletionException e) {
            throw unwrapCompletionException(e);
        }
    }

    /**
     * Acquires a permit from the semaphore, blocking if necessary.
     *
     * @param semaphore the semaphore to acquire from
     * @throws IllegalStateException if interrupted while waiting
     */
    private static void acquirePermit(Semaphore semaphore) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for execution permit", e);
        }
    }

    /**
     * Unwraps the cause of a CompletionException.
     *
     * @param e the completion exception
     * @return the unwrapped runtime exception
     */
    private static RuntimeException unwrapCompletionException(CompletionException e) {
        var cause = e.getCause();
        if (cause instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        return new IllegalStateException("Parallel execution failed", cause);
    }
}
