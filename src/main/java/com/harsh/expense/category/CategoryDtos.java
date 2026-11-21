package com.harsh.expense.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CategoryDtos {
  private CategoryDtos() {}

  @Schema(description = "Request body for creating an expense category.")
  public record Request(
      @Schema(
              description = "Category name (must be unique per user).",
              example = "Food",
              maxLength = 50,
              requiredMode = Schema.RequiredMode.REQUIRED)
          @NotBlank
          @Size(max = 50)
          String name) {}

  @Schema(description = "Expense category details.")
  public record Response(
      @Schema(description = "Category id.", example = "1") Long id,
      @Schema(description = "Category name.", example = "Food") String name) {
    static Response from(Category c) {
      return new Response(c.getId(), c.getName());
    }
  }
}
