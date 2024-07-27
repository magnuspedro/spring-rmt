package br.com.magnus.detectionandrefactoring.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "queue")
public record QueueProperties(String detectPattern, String measurePattern) {
}
