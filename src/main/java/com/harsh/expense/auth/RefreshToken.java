/* Copyright (c) 2026 Harsh (git4harsh). All rights reserved. */
package com.harsh.expense.auth;

import com.harsh.expense.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "refresh_tokens",
    indexes = {
      @Index(name = "idx_refresh_token_hash", columnList = "tokenHash", unique = true),
      @Index(name = "idx_refresh_token_user", columnList = "user_id")
    })
public class RefreshToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  @Column(nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  @Column private Instant revokedAt;

  @Column(length = 64)
  private String replacedByTokenHash;

  @Column(length = 40)
  private String reason;

  protected RefreshToken() {}

  public RefreshToken(AppUser user, String tokenHash, Instant expiresAt) {
    this.user = user;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
  }

  public Long getId() {
    return id;
  }

  public AppUser getUser() {
    return user;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getRevokedAt() {
    return revokedAt;
  }

  public String getReplacedByTokenHash() {
    return replacedByTokenHash;
  }

  public String getReason() {
    return reason;
  }

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  public boolean isActive() {
    return revokedAt == null && !isExpired();
  }

  public void revoke(String reason, String replacedByTokenHash) {
    this.revokedAt = Instant.now();
    this.reason = reason;
    this.replacedByTokenHash = replacedByTokenHash;
  }
}
