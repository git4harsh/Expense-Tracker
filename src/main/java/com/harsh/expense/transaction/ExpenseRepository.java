/* Copyright (c) 2026 Harsh (git4harsh). All rights reserved. */
package com.harsh.expense.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository
    extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
  List<Expense> findAllByOwnerEmailOrderByExpenseDateDescIdDesc(String email);

  Optional<Expense> findByIdAndOwnerEmail(Long id, String email);

  @Query(
      "select coalesce(sum(e.amount), 0) from Expense e where lower(e.owner.email) = lower(:email) and e.expenseDate between :start and :end")
  BigDecimal totalForPeriod(
      @Param("email") String email, @Param("start") LocalDate start, @Param("end") LocalDate end);

  @Query(
      "select coalesce(c.name, 'Uncategorized'), sum(e.amount) from Expense e left join e.category c "
          + "where lower(e.owner.email) = lower(:email) and e.expenseDate between :start and :end "
          + "group by coalesce(c.name, 'Uncategorized') order by sum(e.amount) desc")
  List<Object[]> totalsByCategory(
      @Param("email") String email, @Param("start") LocalDate start, @Param("end") LocalDate end);

  @Query(
      "select month(e.expenseDate), coalesce(sum(e.amount), 0), count(e) from Expense e "
          + "where lower(e.owner.email) = lower(:email) and year(e.expenseDate) = :year "
          + "group by month(e.expenseDate) order by month(e.expenseDate)")
  List<Object[]> totalsByMonth(@Param("email") String email, @Param("year") int year);

  @Query(
      "select count(e) from Expense e where lower(e.owner.email) = lower(:email) and e.expenseDate between :start and :end")
  long countForPeriod(
      @Param("email") String email, @Param("start") LocalDate start, @Param("end") LocalDate end);

  List<Expense> findAllByOwnerEmailAndExpenseDateBetweenOrderByExpenseDateDesc(
      String email, LocalDate start, LocalDate end);
}
