/* Copyright (c) 2026 Harsh (git4harsh). All rights reserved. */
package com.harsh.expense.transaction;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@SecurityRequirement(name = "bearerAuth")
public class SummaryController {
  private final ExpenseService service;

  public SummaryController(ExpenseService service) {
    this.service = service;
  }

  @GetMapping("/monthly")
  public ExpenseDtos.MonthlySummary monthly(
      @Parameter(description = "Report year.", example = "2026", required = true) @RequestParam
          int year,
      @Parameter(description = "Report month (1-12).", example = "11", required = true)
          @RequestParam
          int month,
      @Parameter(hidden = true) Authentication auth) {
    return service.summary(year, month, auth.getName());
  }

  @GetMapping("/yearly")
  public ExpenseDtos.YearlySummary yearly(
      @Parameter(description = "Report year.", example = "2026", required = true) @RequestParam
          int year,
      @Parameter(hidden = true) Authentication auth) {
    return service.yearlySummary(year, auth.getName());
  }
}
