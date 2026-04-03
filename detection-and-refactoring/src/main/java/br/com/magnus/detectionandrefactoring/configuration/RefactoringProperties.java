package br.com.magnus.detectionandrefactoring.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "refactoring")
public record RefactoringProperties(int parallelism) {
}
