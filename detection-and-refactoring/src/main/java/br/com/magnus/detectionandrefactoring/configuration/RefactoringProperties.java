package br.com.magnus.detectionandrefactoring.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Properties for refactoring executor configurations.
 * <p>
 * Parallelism values control the maximum number of concurrent tasks for each executor.
 * Defaults are designed for resource-intensive code analysis operations.
 */
@ConfigurationProperties(prefix = "refactoring")
@Getter
@Setter
public class RefactoringProperties {

    /**
     * Default parallelism for all executors if not specifically overridden.
     */
    @DefaultValue("8")
    private int defaultParallelism = 8;

    /**
     * Executor-specific parallelism overrides.
     * If an executor is not listed here, it uses {@link #defaultParallelism}.
     */
    private Map<String, Integer> executors = new HashMap<>();

    /**
     * Get parallelism for a specific executor, or default if not configured.
     */
    public int getParallelism(String executorName) {
        return executors.getOrDefault(executorName, defaultParallelism);
    }

    /**
     * Legacy compatibility - detection methods manager has lower default parallelism.
     */
    public int getDetectionMethodsManagerParallelism() {
        return getParallelism("detection-methods-manager");
    }

    /**
     * Legacy compatibility - Cinneide executor parallelism.
     */
    public int getCinneideParallelism() {
        return getParallelism("cinneide");
    }

    /**
     * Legacy compatibility - Wei executor parallelism.
     */
    public int getWeiParallelism() {
        return getParallelism("wei");
    }

    /**
     * Legacy compatibility - Zafeiris executor parallelism.
     */
    public int getZafeirisParallelism() {
        return getParallelism("zafeiris");
    }
}
