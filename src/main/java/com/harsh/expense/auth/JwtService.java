package com.harsh.expense.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey key;
  private final long accessExpirationMinutes;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.access-expiration-minutes:${app.jwt.expiration-minutes:15}}")
          long accessExpirationMinutes) {
    if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
      throw new IllegalArgumentException("app.jwt.secret must contain at least 32 bytes");
    }
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessExpirationMinutes = accessExpirationMinutes;
  }

  public String generateAccessToken(String email) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(email)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(accessExpirationMinutes * 60)))
        .claim("typ", "access")
        .signWith(key)
        .compact();
  }

  /** Backward-compatible alias for access token generation. */
  public String generate(String email) {
    return generateAccessToken(email);
  }

  public long getAccessExpirationSeconds() {
    return accessExpirationMinutes * 60;
  }

  public String extractEmail(String token) {
    return parseClaims(token).getSubject();
  }

  public Instant extractExpiration(String token) {
    return parseClaims(token).getExpiration().toInstant();
  }

  private Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}
