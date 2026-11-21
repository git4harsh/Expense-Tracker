/* Copyright (c) 2026 Harsh (git4harsh). All rights reserved. */
package com.harsh.expense.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class ExpenseDtos {
  private ExpenseDtos() {}

  @Schema(description = "Request body for creating or updating an expense.")
  public record Request(
      @Schema(
              description = "Short title for the expense.",
              example = "Lunch",
              maxLength = 120,
              requiredMode = Schema.RequiredMode.REQUIRED)
          @NotBlank
          @Size(max = 120)
          String title,
      @Schema(
              description = "Optional details about the expense.",
              example = "Lunch with colleagues",
              maxLength = 1000,
              requiredMode = Schema.RequiredMode.NOT_REQUIRED,
              nullable = true)
          @Size(max = 1000)
          String description,
      @Schema(
              description =
                  "Expense amount (must be >= 0.01, up to 10 integer digits and 2 decimal places).",
              example = "250.00",
              requiredMode = Schema.RequiredMode.REQUIRED)
          @NotNull
          @DecimalMin(value = "0.01")
          @Digits(integer = 10, fraction = 2)
          BigDecimal amount,
      @Schema(
              description = "Date the expense occurred (ISO-8601).",
              example = "2026-11-21",
              type = "string",
              format = "date",
              requiredMode = Schema.RequiredMode.REQUIRED)
          @NotNull
          LocalDate expenseDate,
      @Schema(
              description =
                  "Optional id of an existing category owned by the authenticated user. Omit or set null if uncategorized.",
              example = "1",
              requiredMode = Schema.RequiredMode.NOT_REQUIRED,
              nullable = true)
          Long categoryId) {}

  @Schema(description = "Expense details returned by the API.")
  public record Response(
      @Schema(description = "Expense id.", example = "10") Long id,
      @Schema(description = "Expense title.", example = "Lunch") String title,
      @Schema(
              description = "Expense description.",
              example = "Lunch with colleagues",
              nullable = true)
          String description,
      @Schema(description = "Expense amount.", example = "250.00") BigDecimal amount,
      @Schema(
              description = "Expense date.",
              example = "2026-11-21",
              type = "string",
              format = "date")
          LocalDate expenseDate,
      @Schema(description = "Category id, if assigned.", example = "1", nullable = true)
          Long categoryId,
      @Schema(description = "Category name, if assigned.", example = "Food", nullable = true)
          String categoryName) {
    static Response from(Expense e) {
      return new Response(
          e.getId(),
          e.getTitle(),
          e.getDescription(),
          e.getAmount(),
          e.getExpenseDate(),
          e.getCategory() == null ? null : e.getCategory().getId(),
          e.getCategory() == null ? null : e.getCategory().getName());
    }
  }

  @Schema(description = "Total spending for a single category in a monthly report.")
  public record CategoryTotal(
      @Schema(
              description = "Category name (or 'Uncategorized' when not assigned).",
              example = "Food")
          String category,
      @Schema(description = "Total amount spent in this category.", example = "250.00")
          BigDecimal total) {}

  @Schema(description = "Monthly spending summary for the authenticated user.")
  public record MonthlySummary(
      @Schema(description = "Report year.", example = "2026") int year,
      @Schema(description = "Report month (1-12).", example = "11") int month,
      @Schema(description = "Total amount spent in the month.", example = "250.00")
          BigDecimal total,
      @Schema(description = "Number of expense transactions in the month.", example = "1")
          long transactionCount,
      @Schema(description = "Totals grouped by category.")
          java.util.List<CategoryTotal> categoryTotals) {}

  @Schema(description = "Spending total for a single month in a yearly report.")
  public record MonthTotal(
      @Schema(description = "Month number (1-12).", example = "11") int month,
      @Schema(description = "Total amount spent in the month.", example = "250.00")
          BigDecimal total,
      @Schema(description = "Number of expense transactions in the month.", example = "1")
          long transactionCount) {}

  @Schema(description = "Yearly spending summary for the authenticated user.")
  public record YearlySummary(
      @Schema(description = "Report year.", example = "2026") int year,
      @Schema(description = "Total amount spent in the year.", example = "1250.00")
          BigDecimal total,
      @Schema(description = "Number of expense transactions in the year.", example = "5")
          long transactionCount,
      @Schema(description = "Monthly totals for the year.")
          java.util.List<MonthTotal> monthlyTotals,
      @Schema(description = "Totals grouped by category for the year.")
          java.util.List<CategoryTotal> categoryTotals) {}

  @Schema(description = "Paginated list of expenses.")
  public record ExpensePage(
      @Schema(description = "Expenses in the current page.") java.util.List<Response> items,
      @Schema(description = "Current page number (0-based).", example = "0") int page,
      @Schema(description = "Maximum number of items per page.", example = "20") int size,
      @Schema(description = "Total number of matching expenses.", example = "42") long totalItems,
      @Schema(description = "Total number of pages.", example = "3") int totalPages) {}
}
