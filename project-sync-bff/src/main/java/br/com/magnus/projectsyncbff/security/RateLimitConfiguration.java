package br.com.magnus.projectsyncbff.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class RateLimitConfiguration {

    @Bean
    public Clock rateLimitClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnProperty(value = "security.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitProperties properties, Clock rateLimitClock) {
        var registration = new FilterRegistrationBean<>(new RateLimitFilter(properties, rateLimitClock));
        registration.addUrlPatterns("/upload", "/rmt/api/v1/upload");
        registration.setOrder(1);
        return registration;
    }
}
