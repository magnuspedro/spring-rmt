package br.com.intermediary.intermediaryagent.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sqs")
public record SqsProperties(String detectPattern) {
}
