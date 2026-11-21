/* Copyright (c) 2026 Harsh (git4harsh). All rights reserved. */
package com.harsh.expense.user;

import com.harsh.expense.common.SecurityAuditEvent;
import com.harsh.expense.common.SecurityAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
  private static final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final SecurityAuditRepository audit;

  public UserService(UserRepository users, PasswordEncoder encoder, SecurityAuditRepository audit) {
    this.users = users;
    this.encoder = encoder;
    this.audit = audit;
  }

  @Transactional(readOnly = true)
  public UserDtos.ProfileResponse getProfile(String email) {
    AppUser user =
        users
            .findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    return UserDtos.ProfileResponse.from(user);
  }

  @Transactional
  public UserDtos.ProfileResponse updateProfile(
      UserDtos.UpdateProfileRequest request, String email, String ipAddress) {
    AppUser user =
        users
            .findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    user.setName(request.name().trim());
    audit.save(
        new SecurityAuditEvent(
            "PROFILE_UPDATE", user.getEmail(), ipAddress, true, "Profile name updated"));
    return UserDtos.ProfileResponse.from(user);
  }

  @Transactional
  public void changePassword(
      UserDtos.ChangePasswordRequest request, String email, String ipAddress) {
    AppUser user =
        users
            .findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (!encoder.matches(request.currentPassword(), user.getPasswordHash())) {
      audit.save(
          new SecurityAuditEvent(
              "PASSWORD_CHANGE", user.getEmail(), ipAddress, false, "Incorrect current password"));
      throw new IllegalArgumentException("Current password is incorrect");
    }
    if (encoder.matches(request.newPassword(), user.getPasswordHash())) {
      audit.save(
          new SecurityAuditEvent(
              "PASSWORD_CHANGE",
              user.getEmail(),
              ipAddress,
              false,
              "New password same as current"));
      throw new IllegalArgumentException(
          "New password must be different from the current password");
    }
    user.setPasswordHash(encoder.encode(request.newPassword()));
    audit.save(
        new SecurityAuditEvent(
            "PASSWORD_CHANGE", user.getEmail(), ipAddress, true, "Password changed"));
    log.info("Password changed for user {}", user.getEmail());
  }
}
