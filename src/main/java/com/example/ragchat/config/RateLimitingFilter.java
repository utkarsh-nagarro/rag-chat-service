package com.example.ragchat.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final Map<String, Window> buckets = new ConcurrentHashMap<>();

    @Value("${rate-limiting.requests-per-minute}")
    private int requestsPerMinute;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-ui.html");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String key = request.getRemoteAddr();
        long now = Instant.now().getEpochSecond();

        Window window = buckets.computeIfAbsent(key, k -> new Window(now, new AtomicInteger(0)));

        synchronized (window) {
            if (now - window.startTimeSeconds >= 60) {
                window.startTimeSeconds = now;
                window.counter.set(0);
            }

            int current = window.counter.incrementAndGet();
            if (current > requestsPerMinute) {
                log.warn("Rate limit exceeded for key {} path {}", key, request.getRequestURI());
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Rate limit exceeded");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private static class Window {
        volatile long startTimeSeconds;
        final AtomicInteger counter;

        Window(long startTimeSeconds, AtomicInteger counter) {
            this.startTimeSeconds = startTimeSeconds;
            this.counter = counter;
        }
    }
}

