/* Copyright (c) 2026 Harsh (git4harsh). All rights reserved. */
package com.harsh.expense.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExpenseCreatedEvent(
    Long expenseId,
    Long userId,
    String userEmail,
    String title,
    BigDecimal amount,
    LocalDate expenseDate,
    Instant occurredAt) {}
