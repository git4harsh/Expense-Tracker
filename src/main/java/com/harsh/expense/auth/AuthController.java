package com.harsh.expense.auth;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService service;

  public AuthController(AuthService service) {
    this.service = service;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Register a new user",
      description = "Creates a user account and returns short-lived access and refresh tokens.")
  public AuthDtos.AuthResponse register(
      @Valid @RequestBody AuthDtos.RegisterRequest request, HttpServletRequest http) {
    return service.register(request, clientIp(http));
  }

  @PostMapping("/login")
  @Operation(
      summary = "Login",
      description = "Authenticates a user and returns short-lived access and refresh tokens.")
  public AuthDtos.AuthResponse login(
      @Valid @RequestBody AuthDtos.LoginRequest request, HttpServletRequest http) {
    return service.login(request, clientIp(http));
  }

  @PostMapping("/refresh")
  @Operation(
      summary = "Refresh access token",
      description = "Rotates the refresh token and returns a new access/refresh token pair.")
  public AuthDtos.AuthResponse refresh(
      @Valid @RequestBody AuthDtos.RefreshRequest request, HttpServletRequest http) {
    return service.refresh(request, clientIp(http));
  }

  @PostMapping("/logout")
  @Operation(
      summary = "Logout / revoke tokens",
      description =
          "Revokes a refresh token. Use allDevices=true with Authorization to revoke all active refresh tokens for the user.")
  public AuthDtos.MessageResponse logout(
      @RequestBody(required = false) AuthDtos.LogoutRequest request,
      Authentication authentication,
      HttpServletRequest http) {
    String email = authentication == null ? null : authentication.getName();
    return service.logout(request, email, clientIp(http));
  }

  private String clientIp(HttpServletRequest request) {
    if (request == null) return null;
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
