package br.com.magnus.detectionandrefactoring.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "refactoring")
@Getter
@Setter
public class RefactoringProperties {
    private ExecutorProperties detectionMethodsManager = new ExecutorProperties(3);
    private ExecutorProperties cinneide = new ExecutorProperties(8);
    private ExecutorProperties wei = new ExecutorProperties(8);
    private ExecutorProperties zafeiris = new ExecutorProperties(8);

    @Getter
    @Setter
    public static class ExecutorProperties {
        private int parallelism = 1;

        public ExecutorProperties() {
        }

        public ExecutorProperties(int parallelism) {
            this.parallelism = parallelism;
        }
    }
}
