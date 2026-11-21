package com.harsh.expense.auth;

import com.harsh.expense.user.AppUser;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {
  private final RefreshTokenRepository tokens;
  private final long refreshExpirationDays;
  private final SecureRandom random = new SecureRandom();

  public RefreshTokenService(
      RefreshTokenRepository tokens,
      @Value("${app.jwt.refresh-expiration-days:30}") long refreshExpirationDays) {
    this.tokens = tokens;
    this.refreshExpirationDays = refreshExpirationDays;
  }

  @Transactional
  public String issue(AppUser user) {
    String raw = generateRawToken();
    String hash = hash(raw);
    Instant expiresAt = Instant.now().plus(refreshExpirationDays, ChronoUnit.DAYS);
    tokens.save(new RefreshToken(user, hash, expiresAt));
    return raw;
  }

  @Transactional
  public RotationResult rotate(String rawRefreshToken) {
    String hash = hash(rawRefreshToken);
    RefreshToken existing =
        tokens
            .findByTokenHash(hash)
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired refresh token"));
    if (!existing.isActive()) {
      // Reuse detection: if already rotated/revoked, revoke all active tokens for safety.
      if (existing.getRevokedAt() != null) {
        tokens.revokeAllActiveForUser(existing.getUser(), Instant.now(), "reuse-detected");
      }
      throw new IllegalArgumentException("Invalid or expired refresh token");
    }
    String newRaw = generateRawToken();
    String newHash = hash(newRaw);
    existing.revoke("rotated", newHash);
    tokens.save(existing);
    Instant expiresAt = Instant.now().plus(refreshExpirationDays, ChronoUnit.DAYS);
    tokens.save(new RefreshToken(existing.getUser(), newHash, expiresAt));
    return new RotationResult(existing.getUser(), newRaw);
  }

  @Transactional
  public void revoke(String rawRefreshToken) {
    if (rawRefreshToken == null || rawRefreshToken.isBlank()) return;
    String hash = hash(rawRefreshToken);
    Optional<RefreshToken> token = tokens.findByTokenHash(hash);
    token.ifPresent(
        t -> {
          if (t.getRevokedAt() == null) {
            t.revoke("logout", null);
            tokens.save(t);
          }
        });
  }

  @Transactional
  public int revokeAllForUser(AppUser user) {
    return tokens.revokeAllActiveForUser(user, Instant.now(), "logout-all");
  }

  private String generateRawToken() {
    byte[] bytes = new byte[48];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String hash(String raw) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hashed);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  public record RotationResult(AppUser user, String newRefreshToken) {}
}
