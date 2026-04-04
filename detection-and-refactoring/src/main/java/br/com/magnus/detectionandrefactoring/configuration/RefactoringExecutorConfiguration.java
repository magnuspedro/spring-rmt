package br.com.magnus.detectionandrefactoring.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Configures bounded thread pool executors for refactoring operations.
 * <p>
 * Each executor has a fixed core/max pool size based on configured parallelism.
 * Bounded executors are more appropriate than virtual threads for CPU-intensive
 * code analysis tasks, as they naturally limit resource usage.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RefactoringExecutorConfiguration {

    private final RefactoringProperties refactoringProperties;

    private static final int QUEUE_CAPACITY = 100;
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 30;

    @Bean(destroyMethod = "shutdownExecutor")
    public ExecutorService detectionMethodsManagerExecutor() {
        return createBoundedExecutor(
                "detection-methods-manager",
                refactoringProperties.getParallelism("detection-methods-manager")
        );
    }

    @Bean(destroyMethod = "shutdownExecutor")
    public ExecutorService cinneideRefactoringExecutor() {
        return createBoundedExecutor(
                "cinneide-refactoring",
                refactoringProperties.getParallelism("cinneide")
        );
    }

    @Bean(destroyMethod = "shutdownExecutor")
    public ExecutorService weiRefactoringExecutor() {
        return createBoundedExecutor(
                "wei-refactoring",
                refactoringProperties.getParallelism("wei")
        );
    }

    @Bean(destroyMethod = "shutdownExecutor")
    public ExecutorService zafeirisRefactoringExecutor() {
        return createBoundedExecutor(
                "zafeiris-refactoring",
                refactoringProperties.getParallelism("zafeiris")
        );
    }

    /**
     * Creates a bounded thread pool executor with the given pool size.
     * <p>
     * The executor will:
     * <ul>
     *   <li>Use exactly {@code poolSize} threads</li>
     *   <li>Queue up to {@code QUEUE_CAPACITY} tasks when all threads are busy</li>
     *   <li>Reject tasks (throws RejectedExecutionException) when queue is full</li>
     * </ul>
     *
     * @param namePrefix prefix for thread names
     * @param poolSize   fixed number of threads in the pool
     * @return a configured ExecutorService
     */
    private ExecutorService createBoundedExecutor(String namePrefix, int poolSize) {
        log.info("Creating executor '{}' with pool size: {}", namePrefix, poolSize);

        return new ThreadPoolExecutor(
                poolSize,
                poolSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.DiscardPolicy() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                        log.warn("Task rejected by executor '{}': queue full (capacity: {})",
                                namePrefix, QUEUE_CAPACITY);
                        super.rejectedExecution(r, e);
                    }
                }
        ) {
            @Override
            public String toString() {
                return String.format("%s[pool=%d, active=%d, queued=%d]",
                        namePrefix, getPoolSize(), getActiveCount(), getQueue().size());
            }
        };
    }

    /**
     * Custom destroy method that logs executor state before shutdown.
     */
    @SuppressWarnings("unused")
    private void shutdownExecutor(ExecutorService executor) {
        log.info("Shutting down executor: {}", executor);
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                log.warn("Executor did not terminate in {}s, forcing shutdown", SHUTDOWN_TIMEOUT_SECONDS);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
