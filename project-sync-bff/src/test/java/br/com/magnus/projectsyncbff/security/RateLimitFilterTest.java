package br.com.magnus.projectsyncbff.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    @Test
    @DisplayName("Should return 429 after exceeding the configured request limit")
    void shouldReturn429AfterExceedingRequestLimit() throws Exception {
        var properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setMaxRequestsPerMinute(1);
        var filter = new RateLimitFilter(properties, Clock.fixed(Instant.parse("2026-04-03T12:00:00Z"), ZoneOffset.UTC));
        var request = new MockHttpServletRequest("POST", "/upload");
        request.setRemoteAddr("127.0.0.1");

        var firstResponse = new MockHttpServletResponse();
        filter.doFilter(request, firstResponse, new MockFilterChain());

        var secondResponse = new MockHttpServletResponse();
        filter.doFilter(request, secondResponse, new MockFilterChain());

        assertThat(firstResponse.getStatus()).isEqualTo(200);
        assertThat(secondResponse.getStatus()).isEqualTo(429);
    }
}
