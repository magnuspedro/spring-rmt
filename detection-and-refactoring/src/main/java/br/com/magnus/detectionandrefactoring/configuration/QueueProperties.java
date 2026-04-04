package br.com.magnus.detectionandrefactoring.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for queue configuration.
 * <p>
 * Contains the queue name patterns for detection and measurement operations.
 *
 * @param detectPattern the queue pattern for detection operations
 * @param measurePattern the queue pattern for measurement operations
 */
@ConfigurationProperties(prefix = "queue")
public record QueueProperties(String detectPattern, String measurePattern) {
}
