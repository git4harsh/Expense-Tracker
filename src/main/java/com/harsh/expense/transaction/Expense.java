/* Copyright (c) 2026 Harsh (git4harsh). All rights reserved. */
package com.harsh.expense.transaction;

import com.harsh.expense.category.Category;
import com.harsh.expense.user.AppUser;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
public class Expense {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120)
  private String title;

  @Column(length = 1000)
  private String description;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false)
  private LocalDate expenseDate;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  private AppUser owner;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  protected Expense() {}

  public Expense(
      String title,
      String description,
      BigDecimal amount,
      LocalDate expenseDate,
      AppUser owner,
      Category category) {
    this.title = title;
    this.description = description;
    this.amount = amount;
    this.expenseDate = expenseDate;
    this.owner = owner;
    this.category = category;
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public LocalDate getExpenseDate() {
    return expenseDate;
  }

  public AppUser getOwner() {
    return owner;
  }

  public Category getCategory() {
    return category;
  }

  public void update(
      String title, String description, BigDecimal amount, LocalDate date, Category category) {
    this.title = title;
    this.description = description;
    this.amount = amount;
    this.expenseDate = date;
    this.category = category;
  }
}
