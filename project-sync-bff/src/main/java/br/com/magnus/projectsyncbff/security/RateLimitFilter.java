package br.com.magnus.projectsyncbff.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
    private static final long ONE_MINUTE_IN_MILLIS = 60_000L;

    private final RateLimitProperties properties;
    private final Clock clock;
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        var now = clock.millis();
        evictStaleEntries(now);
        var clientIp = clientIp(request);
        var bucket = buckets.computeIfAbsent(clientIp, ignored -> new TokenBucket(properties.getMaxRequestsPerMinute(), now));
        if (!bucket.tryConsume(now, properties.getMaxRequestsPerMinute())) {
            response.sendError(429, "Rate limit exceeded");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void evictStaleEntries(long now) {
        buckets.entrySet().removeIf(entry -> entry.getValue().isStale(now, properties.getMaxRequestsPerMinute()));
    }

    private String clientIp(HttpServletRequest request) {
        var forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    static final class TokenBucket {
        private int tokens;
        private long windowStart;
        private long lastAccessed;

        private TokenBucket(int tokens, long now) {
            this.tokens = tokens;
            this.windowStart = now;
            this.lastAccessed = now;
        }

        private synchronized boolean tryConsume(long now, int maxTokens) {
            refill(now, maxTokens);
            lastAccessed = now;
            if (tokens <= 0) {
                return false;
            }
            tokens--;
            return true;
        }

        private synchronized boolean isStale(long now, int maxTokens) {
            refill(now, maxTokens);
            return tokens == maxTokens && now - lastAccessed >= ONE_MINUTE_IN_MILLIS;
        }

        private void refill(long now, int maxTokens) {
            if (now - windowStart >= ONE_MINUTE_IN_MILLIS) {
                tokens = maxTokens;
                windowStart = now;
            }
        }
    }
}
