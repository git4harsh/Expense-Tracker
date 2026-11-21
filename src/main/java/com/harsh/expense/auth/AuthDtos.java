/* Copyright (c) 2026 Harsh (git4harsh). All rights reserved. */
package com.harsh.expense.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
  private AuthDtos() {}

  @Schema(description = "Request body for creating a new user account.")
  public record RegisterRequest(
      @Schema(
              description = "Full name of the user.",
              example = "Harsh",
              maxLength = 80,
              requiredMode = Schema.RequiredMode.REQUIRED)
          @NotBlank
          @Size(max = 80)
          String name,
      @Schema(
              description = "Unique email address used for login.",
              example = "harsh@example.com",
              maxLength = 120,
              requiredMode = Schema.RequiredMode.REQUIRED)
          @NotBlank
          @Email
          @Size(max = 120)
          String email,
      @Schema(
              description = "Password for the account (8-72 characters).",
              example = "password123",
              minLength = 8,
              maxLength = 72,
              requiredMode = Schema.RequiredMode.REQUIRED)
          @NotBlank
          @Size(min = 8, max = 72)
          String password) {}

  @Schema(description = "Request body for authenticating an existing user.")
  public record LoginRequest(
      @Schema(
              description = "Registered email address.",
              example = "harsh@example.com",
              requiredMode = Schema.RequiredMode.REQUIRED)
          @NotBlank
          @Email
          String email,
      @Schema(
              description = "Account password.",
              example = "password123",
              requiredMode = Schema.RequiredMode.REQUIRED)
          @NotBlank
          String password) {}

  @Schema(description = "Request body for exchanging/rotating a refresh token.")
  public record RefreshRequest(
      @Schema(
              description = "Opaque refresh token issued by login/register/refresh.",
              requiredMode = Schema.RequiredMode.REQUIRED)
          @NotBlank
          String refreshToken) {}

  @Schema(description = "Request body for logging out and revoking a refresh token.")
  public record LogoutRequest(
      @Schema(
              description =
                  "Refresh token to revoke. If omitted, server may revoke all tokens for the authenticated context where applicable.",
              nullable = true)
          String refreshToken,
      @Schema(
              description =
                  "When true, revoke all refresh tokens for the authenticated user (requires Authorization).",
              example = "false")
          Boolean allDevices) {}

  @Schema(
      description =
          "Authentication response containing access and refresh tokens and basic user details.")
  public record AuthResponse(
      @Schema(
              description =
                  "JWT access token (short-lived) to send as 'Authorization: Bearer <token>'.")
          String accessToken,
      @Schema(description = "Access token lifetime in seconds.", example = "900")
          long expiresInSeconds,
      @Schema(
              description = "Opaque refresh token (long-lived) used to obtain a new access token.",
              nullable = true)
          String refreshToken,
      @Schema(description = "Token type for the Authorization header.", example = "Bearer")
          String tokenType,
      @Schema(description = "Authenticated user's id.", example = "1") Long userId,
      @Schema(description = "Authenticated user's name.", example = "Harsh") String name,
      @Schema(description = "Authenticated user's email.", example = "harsh@example.com")
          String email) {}

  @Schema(description = "Simple message response.")
  public record MessageResponse(
      @Schema(description = "Human-readable message.", example = "Logged out successfully")
          String message) {}
}
