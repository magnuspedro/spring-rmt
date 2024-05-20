package br.com.magnus.refactoringandmetricsmanager.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sqs")
public record SqsProperties(String detectPattern) {
}
