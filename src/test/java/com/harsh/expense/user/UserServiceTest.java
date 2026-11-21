/* Copyright (c) 2026 Harsh (git4harsh). All rights reserved. */
package com.harsh.expense.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.harsh.expense.common.SecurityAuditEvent;
import com.harsh.expense.common.SecurityAuditRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock UserRepository users;
  @Mock PasswordEncoder encoder;
  @Mock SecurityAuditRepository audit;
  @InjectMocks UserService service;

  @Test
  void updatesProfileNameForAuthenticatedUser() {
    AppUser user = new AppUser("owner@example.com", "Owner", "hash");
    when(users.findByEmailIgnoreCase("owner@example.com")).thenReturn(Optional.of(user));
    when(audit.save(any(SecurityAuditEvent.class))).thenAnswer(inv -> inv.getArgument(0));

    var result =
        service.updateProfile(
            new UserDtos.UpdateProfileRequest("New Name"), "owner@example.com", "127.0.0.1");

    assertEquals("New Name", result.name());
    assertEquals("owner@example.com", result.email());
    assertEquals("New Name", user.getName());
    verify(audit).save(any(SecurityAuditEvent.class));
  }

  @Test
  void rejectsPasswordChangeWhenCurrentPasswordIsIncorrect() {
    AppUser user = new AppUser("owner@example.com", "Owner", "hash");
    when(users.findByEmailIgnoreCase("owner@example.com")).thenReturn(Optional.of(user));
    when(encoder.matches("wrong", "hash")).thenReturn(false);
    when(audit.save(any(SecurityAuditEvent.class))).thenAnswer(inv -> inv.getArgument(0));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            service.changePassword(
                new UserDtos.ChangePasswordRequest("wrong", "newPassword123"),
                "owner@example.com",
                "127.0.0.1"));
    verify(encoder, never()).encode(anyString());
    verify(audit).save(any(SecurityAuditEvent.class));
  }

  @Test
  void rejectsPasswordChangeWhenNewPasswordMatchesCurrentPassword() {
    AppUser user = new AppUser("owner@example.com", "Owner", "hash");
    when(users.findByEmailIgnoreCase("owner@example.com")).thenReturn(Optional.of(user));
    when(encoder.matches("samePassword", "hash")).thenReturn(true);
    when(audit.save(any(SecurityAuditEvent.class))).thenAnswer(inv -> inv.getArgument(0));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            service.changePassword(
                new UserDtos.ChangePasswordRequest("samePassword", "samePassword"),
                "owner@example.com",
                "127.0.0.1"));
    verify(encoder, never()).encode(anyString());
    verify(audit).save(any(SecurityAuditEvent.class));
  }

  @Test
  void encodesAndStoresNewPasswordWhenCurrentPasswordIsValid() {
    AppUser user = new AppUser("owner@example.com", "Owner", "old-hash");
    when(users.findByEmailIgnoreCase("owner@example.com")).thenReturn(Optional.of(user));
    when(encoder.matches("currentPassword", "old-hash")).thenReturn(true);
    when(encoder.matches("newPassword123", "old-hash")).thenReturn(false);
    when(encoder.encode("newPassword123")).thenReturn("new-hash");
    when(audit.save(any(SecurityAuditEvent.class))).thenAnswer(inv -> inv.getArgument(0));

    service.changePassword(
        new UserDtos.ChangePasswordRequest("currentPassword", "newPassword123"),
        "owner@example.com",
        "127.0.0.1");

    assertEquals("new-hash", user.getPasswordHash());
    verify(audit).save(any(SecurityAuditEvent.class));
  }
}
