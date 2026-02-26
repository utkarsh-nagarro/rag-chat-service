package com.ragchat.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class ApiKeyAuthFilterTest {

    @Test
    void shouldSkipPublicEndpoint() throws Exception {

        ApiKeyAuthFilter filter = new ApiKeyAuthFilter();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/actuator/health");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});

        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldReturn401WhenMissing() throws Exception {

        ApiKeyAuthFilter filter = new ApiKeyAuthFilter();
        ReflectionTestUtils.setField(filter, "expectedApiKey", "valid-key");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/sessions");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});

        assertEquals(401, response.getStatus());
    }

    @Test
    void shouldAllowWhenValid() throws Exception {

        ApiKeyAuthFilter filter = new ApiKeyAuthFilter();
        ReflectionTestUtils.setField(filter, "expectedApiKey", "valid-key");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/sessions");
        request.addHeader("X-API-KEY", "valid-key");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});

        assertNotEquals(401, response.getStatus());
    }
}