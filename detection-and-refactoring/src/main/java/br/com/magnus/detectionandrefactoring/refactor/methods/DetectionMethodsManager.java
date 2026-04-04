package br.com.magnus.detectionandrefactoring.refactor.methods;

import br.com.magnus.config.starter.configuration.JavaParserSingleton;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.projects.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

public interface DetectionMethodsManager {
    List<RefactorFiles> refactor(Project project);

    default boolean hasNoCandidates(List<RefactoringCandidate> candidates) {
        return candidates.isEmpty();
    }

    /**
     * Executes tasks in parallel with bounded concurrency.
     * <p>
     * All tasks are submitted immediately, but a semaphore ensures at most {@code parallelism}
     * tasks execute concurrently. This provides true parallelism control unlike batch-based approaches.
     *
     * @param items      the items to process
     * @param executor   the executor for running tasks
     * @param parallelism the maximum number of concurrent tasks
     * @param task       the function to apply to each item
     * @param <T>        the input type
     * @param <R>        the result type
     * @return list of results in the same order as input items
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

    private static void acquirePermit(Semaphore semaphore) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for execution permit", e);
        }
    }

    private static RuntimeException unwrapCompletionException(CompletionException e) {
        var cause = e.getCause();
        if (cause instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        return new IllegalStateException("Parallel execution failed", cause);
    }
}
