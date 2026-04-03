package br.com.magnus.detectionandrefactoring.refactor.methods;

import br.com.magnus.config.starter.configuration.JavaParserSingleton;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.members.RefactorFiles;
import br.com.magnus.config.starter.projects.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.function.Function;


public interface DetectionMethodsManager {
    List<RefactorFiles> refactor(Project project);

    default boolean hasNoCandidates(List<RefactoringCandidate> candidates) {
        return candidates.isEmpty();
    }

    default <T, R> List<R> executeInParallel(List<T> items, int parallelism, Function<T, R> task) {
        if (items.isEmpty()) {
            return List.of();
        }

        var boundedParallelism = Math.max(1, parallelism);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var results = new ArrayList<R>(items.size());
            for (var start = 0; start < items.size(); start += boundedParallelism) {
                var end = Math.min(start + boundedParallelism, items.size());
                var tasks = items.subList(start, end).stream()
                        .map(item -> CompletableFuture.supplyAsync(
                                () -> JavaParserSingleton.callWithScopedParser(() -> task.apply(item)),
                                executor))
                        .toList();

                results.addAll(tasks.stream()
                        .map(CompletableFuture::join)
                        .toList());
            }
            return new ArrayList<>(results);
        } catch (CompletionException exception) {
            if (exception.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Parallel refactoring failed", exception.getCause());
        }
    }
}
