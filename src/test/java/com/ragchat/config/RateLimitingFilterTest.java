package com.ragchat.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitingFilterTest {

    @Test
    void shouldSkipSwagger() throws Exception {

        RateLimitingFilter filter = new RateLimitingFilter();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/swagger-ui/index.html");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});

        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldBlockWhenExceeded() throws Exception {

        RateLimitingFilter filter = new RateLimitingFilter();

        ReflectionTestUtils.setField(filter, "apiKeyHeader", "X-API-KEY");
        ReflectionTestUtils.setField(filter, "requestsPerMinute", 1);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/sessions");
        request.addHeader("X-API-KEY", "test");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});
        filter.doFilter(request, response, (req, res) -> {});

        assertEquals(429, response.getStatus());
    }
}