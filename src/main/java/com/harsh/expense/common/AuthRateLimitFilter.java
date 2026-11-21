package com.harsh.expense.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

  private final int maxRequests;
  private final long windowSeconds;
  private final ObjectMapper mapper;
  private final ConcurrentHashMap<String, Window> buckets = new ConcurrentHashMap<>();

  public AuthRateLimitFilter(
      @Value("${app.auth-rate-limit.max-requests:20}") int maxRequests,
      @Value("${app.auth-rate-limit.window-seconds:60}") long windowSeconds,
      ObjectMapper mapper) {
    this.maxRequests = Math.max(1, maxRequests);
    this.windowSeconds = Math.max(1, windowSeconds);
    this.mapper = mapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path == null || !path.startsWith("/api/auth/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String key = clientIp(request) + "|" + request.getRequestURI();
    long now = Instant.now().getEpochSecond();
    Window window =
        buckets.compute(
            key,
            (k, existing) -> {
              if (existing == null || now - existing.windowStart >= windowSeconds) {
                return new Window(now, new AtomicInteger(1));
              }
              existing.count.incrementAndGet();
              return existing;
            });

    // Opportunistic cleanup to avoid unbounded growth.
    if (buckets.size() > 10_000) {
      buckets.entrySet().removeIf(e -> now - e.getValue().windowStart > windowSeconds * 2);
    }

    if (window.count.get() > maxRequests) {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setHeader(
          "Retry-After", String.valueOf(Math.max(1, windowSeconds - (now - window.windowStart))));
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("timestamp", Instant.now().toString());
      body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
      body.put("error", HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase());
      body.put("message", "Too many authentication requests. Please try again later.");
      mapper.writeValue(response.getOutputStream(), body);
      return;
    }
    filterChain.doFilter(request, response);
  }

  private String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
  }

  private static class Window {
    final long windowStart;
    final AtomicInteger count;

    Window(long windowStart, AtomicInteger count) {
      this.windowStart = windowStart;
      this.count = count;
    }
  }
}
