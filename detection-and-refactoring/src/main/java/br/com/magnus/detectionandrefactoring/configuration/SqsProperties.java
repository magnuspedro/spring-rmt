package br.com.magnus.detectionandrefactoring.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sqs")
public record SqsProperties(String detectPattern, String measurePattern) {
}
