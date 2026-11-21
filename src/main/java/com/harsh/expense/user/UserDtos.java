/* Copyright (c) 2026 Harsh (git4harsh). All rights reserved. */
package com.harsh.expense.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class UserDtos {
  private UserDtos() {}

  @Schema(description = "Authenticated user's profile.")
  public record ProfileResponse(
      @Schema(description = "User id.", example = "1") Long id,
      @Schema(description = "Full name.", example = "Harsh") String name,
      @Schema(description = "Email address.", example = "harsh@example.com") String email,
      @Schema(description = "Account creation timestamp.", example = "2026-11-21T12:00:00Z")
          Instant createdAt) {
    static ProfileResponse from(AppUser user) {
      return new ProfileResponse(
          user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }
  }

  @Schema(description = "Request body for updating the authenticated user's profile.")
  public record UpdateProfileRequest(
      @Schema(
              description = "Updated full name.",
              example = "Harsh Kumar",
              maxLength = 80,
              requiredMode = Schema.RequiredMode.REQUIRED)
          @NotBlank
          @Size(max = 80)
          String name) {}

  @Schema(description = "Request body for changing the authenticated user's password.")
  public record ChangePasswordRequest(
      @Schema(
              description = "Current account password.",
              example = "password123",
              requiredMode = Schema.RequiredMode.REQUIRED)
          @NotBlank
          String currentPassword,
      @Schema(
              description = "New password (8-72 characters).",
              example = "newPassword123",
              minLength = 8,
              maxLength = 72,
              requiredMode = Schema.RequiredMode.REQUIRED)
          @NotBlank
          @Size(min = 8, max = 72)
          String newPassword) {}
}
