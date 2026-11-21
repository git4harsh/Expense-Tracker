/* Copyright (c) 2026 Harsh (git4harsh). All rights reserved. */
package com.harsh.expense.transaction;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseController {
  private final ExpenseService service;

  public ExpenseController(ExpenseService service) {
    this.service = service;
  }

  @GetMapping
  public ExpenseDtos.ExpensePage list(
      @Parameter(
              description = "Include expenses on/after this date (ISO-8601).",
              example = "2026-11-01")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate from,
      @Parameter(
              description = "Include expenses on/before this date (ISO-8601).",
              example = "2026-11-30")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate to,
      @Parameter(
              description = "Filter by category id owned by the authenticated user.",
              example = "1")
          @RequestParam(required = false)
          Long categoryId,
      @Parameter(
              description = "Case-insensitive search in title and description.",
              example = "lunch")
          @RequestParam(required = false)
          String q,
      @Parameter(description = "Page number (0-based).", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Page size (1-100).", example = "20")
          @RequestParam(defaultValue = "20")
          int size,
      @Parameter(
              description = "Sort field: expenseDate, amount, title, or id.",
              example = "expenseDate")
          @RequestParam(defaultValue = "expenseDate")
          String sortBy,
      @Parameter(description = "Sort direction: asc or desc.", example = "desc")
          @RequestParam(defaultValue = "desc")
          String sortDir,
      @Parameter(hidden = true) Authentication auth) {
    return service.search(auth.getName(), from, to, categoryId, q, page, size, sortBy, sortDir);
  }

  @GetMapping("/{id}")
  public ExpenseDtos.Response get(
      @PathVariable Long id, @Parameter(hidden = true) Authentication auth) {
    return service.get(id, auth.getName());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ExpenseDtos.Response create(
      @Valid @RequestBody ExpenseDtos.Request request,
      @Parameter(hidden = true) Authentication auth) {
    return service.create(request, auth.getName());
  }

  @PutMapping("/{id}")
  public ExpenseDtos.Response update(
      @PathVariable Long id,
      @Valid @RequestBody ExpenseDtos.Request request,
      @Parameter(hidden = true) Authentication auth) {
    return service.update(id, request, auth.getName());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id, @Parameter(hidden = true) Authentication auth) {
    service.delete(id, auth.getName());
  }
}
