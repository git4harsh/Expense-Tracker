/* Copyright (c) 2026 Harsh (git4harsh). All rights reserved. */
package com.harsh.expense.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class JwtServiceTest {
  private static final String TEST_SECRET =
      "unit-test-secret-that-is-long-enough-for-hmac-sha256-123456789";

  @Test
  void generatedTokenContainsTheEmailSubject() {
    JwtService service = new JwtService(TEST_SECRET, 30);
    String token = service.generate("person@example.com");
    assertEquals("person@example.com", service.extractEmail(token));
  }

  @Test
  void rejectsSecretsShorterThanThirtyTwoBytes() {
    assertThrows(IllegalArgumentException.class, () -> new JwtService("too-short", 30));
  }
}
