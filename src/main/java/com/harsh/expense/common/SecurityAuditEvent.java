/* Copyright (c) 2026 Harsh (git4harsh). All rights reserved. */
package com.harsh.expense.common;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "security_audit_events",
    indexes = {
      @Index(name = "idx_audit_user", columnList = "userEmail"),
      @Index(name = "idx_audit_type_time", columnList = "eventType,createdAt")
    })
public class SecurityAuditEvent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 60)
  private String eventType;

  @Column(length = 120)
  private String userEmail;

  @Column(length = 45)
  private String ipAddress;

  @Column(nullable = false)
  private boolean success;

  @Column(length = 500)
  private String details;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  protected SecurityAuditEvent() {}

  public SecurityAuditEvent(
      String eventType, String userEmail, String ipAddress, boolean success, String details) {
    this.eventType = eventType;
    this.userEmail = userEmail;
    this.ipAddress = ipAddress;
    this.success = success;
    this.details = details;
  }

  public Long getId() {
    return id;
  }

  public String getEventType() {
    return eventType;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getDetails() {
    return details;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
