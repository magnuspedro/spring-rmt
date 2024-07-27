package br.com.magnus.projectsyncbff.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "queue")
public record QueueProperties(String detectPattern) {
}
