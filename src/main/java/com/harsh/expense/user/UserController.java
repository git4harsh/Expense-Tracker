/* Copyright (c) 2026 Harsh (git4harsh). All rights reserved. */
package com.harsh.expense.user;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
  private final UserService service;

  public UserController(UserService service) {
    this.service = service;
  }

  @GetMapping
  public UserDtos.ProfileResponse getProfile(@Parameter(hidden = true) Authentication auth) {
    return service.getProfile(auth.getName());
  }

  @PutMapping
  public UserDtos.ProfileResponse updateProfile(
      @Valid @RequestBody UserDtos.UpdateProfileRequest request,
      @Parameter(hidden = true) Authentication auth,
      HttpServletRequest http) {
    return service.updateProfile(request, auth.getName(), clientIp(http));
  }

  @PutMapping("/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void changePassword(
      @Valid @RequestBody UserDtos.ChangePasswordRequest request,
      @Parameter(hidden = true) Authentication auth,
      HttpServletRequest http) {
    service.changePassword(request, auth.getName(), clientIp(http));
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
