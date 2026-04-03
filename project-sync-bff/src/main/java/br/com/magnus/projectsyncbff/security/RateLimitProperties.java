package br.com.magnus.projectsyncbff.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.rate-limit")
public class RateLimitProperties {
    private boolean enabled = true;
    private int maxRequestsPerMinute = 10;
}
