package br.com.detection.detectionagent.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sqs")
public record SqsProperties(String detectPattern, String measurePattern) {
}
