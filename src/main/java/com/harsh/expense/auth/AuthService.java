package com.harsh.expense.auth;

import com.harsh.expense.common.SecurityAuditEvent;
import com.harsh.expense.common.SecurityAuditRepository;
import com.harsh.expense.user.AppUser;
import com.harsh.expense.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private static final Logger log = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final JwtService jwt;
  private final RefreshTokenService refreshTokens;
  private final SecurityAuditRepository audit;

  public AuthService(
      UserRepository users,
      PasswordEncoder encoder,
      JwtService jwt,
      RefreshTokenService refreshTokens,
      SecurityAuditRepository audit) {
    this.users = users;
    this.encoder = encoder;
    this.jwt = jwt;
    this.refreshTokens = refreshTokens;
    this.audit = audit;
  }

  @Transactional
  public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request, String ipAddress) {
    String email = request.email().trim().toLowerCase();
    if (users.existsByEmailIgnoreCase(email)) {
      audit.save(
          new SecurityAuditEvent(
              "AUTH_REGISTER", email, ipAddress, false, "Email already registered"));
      throw new IllegalArgumentException("Email is already registered");
    }
    AppUser user =
        users.save(new AppUser(email, request.name().trim(), encoder.encode(request.password())));
    audit.save(new SecurityAuditEvent("AUTH_REGISTER", email, ipAddress, true, "User registered"));
    log.info("User registered: {}", email);
    return response(user);
  }

  @Transactional
  public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request, String ipAddress) {
    String email = request.email().trim().toLowerCase();
    AppUser user = users.findByEmailIgnoreCase(email).orElse(null);
    if (user == null || !encoder.matches(request.password(), user.getPasswordHash())) {
      audit.save(
          new SecurityAuditEvent("AUTH_LOGIN", email, ipAddress, false, "Invalid credentials"));
      throw new IllegalArgumentException("Invalid email or password");
    }
    audit.save(
        new SecurityAuditEvent("AUTH_LOGIN", user.getEmail(), ipAddress, true, "Login successful"));
    return response(user);
  }

  @Transactional
  public AuthDtos.AuthResponse refresh(AuthDtos.RefreshRequest request, String ipAddress) {
    RefreshTokenService.RotationResult rotated = refreshTokens.rotate(request.refreshToken());
    AppUser user = rotated.user();
    audit.save(
        new SecurityAuditEvent(
            "AUTH_REFRESH", user.getEmail(), ipAddress, true, "Refresh token rotated"));
    return buildResponse(user, rotated.newRefreshToken());
  }

  @Transactional
  public AuthDtos.MessageResponse logout(
      AuthDtos.LogoutRequest request, String authenticatedEmail, String ipAddress) {
    boolean allDevices = request != null && Boolean.TRUE.equals(request.allDevices());
    String refreshToken = request == null ? null : request.refreshToken();
    if (allDevices) {
      if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
        throw new IllegalArgumentException(
            "Authentication is required to log out from all devices");
      }
      AppUser user =
          users
              .findByEmailIgnoreCase(authenticatedEmail)
              .orElseThrow(() -> new IllegalArgumentException("User not found"));
      int revoked = refreshTokens.revokeAllForUser(user);
      audit.save(
          new SecurityAuditEvent(
              "AUTH_LOGOUT_ALL",
              user.getEmail(),
              ipAddress,
              true,
              "Revoked " + revoked + " active refresh token(s)"));
      return new AuthDtos.MessageResponse("Logged out from all devices");
    }
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new IllegalArgumentException("refreshToken is required unless allDevices=true");
    }
    refreshTokens.revoke(refreshToken);
    audit.save(
        new SecurityAuditEvent(
            "AUTH_LOGOUT", authenticatedEmail, ipAddress, true, "Refresh token revoked"));
    return new AuthDtos.MessageResponse("Logged out successfully");
  }

  private AuthDtos.AuthResponse response(AppUser user) {
    String refresh = refreshTokens.issue(user);
    return buildResponse(user, refresh);
  }

  private AuthDtos.AuthResponse buildResponse(AppUser user, String refreshToken) {
    String access = jwt.generateAccessToken(user.getEmail());
    return new AuthDtos.AuthResponse(
        access,
        jwt.getAccessExpirationSeconds(),
        refreshToken,
        "Bearer",
        user.getId(),
        user.getName(),
        user.getEmail());
  }
}
