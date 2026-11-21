package com.harsh.expense.transaction;

import com.harsh.expense.category.CategoryRepository;
import com.harsh.expense.events.ExpenseCreatedEvent;
import com.harsh.expense.events.ExpenseEventPublisher;
import com.harsh.expense.user.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {
  private final ExpenseRepository expenses;
  private final CategoryRepository categories;
  private final UserRepository users;
  private final ExpenseEventPublisher eventPublisher;

  public ExpenseService(
      ExpenseRepository expenses,
      CategoryRepository categories,
      UserRepository users,
      ExpenseEventPublisher eventPublisher) {
    this.expenses = expenses;
    this.categories = categories;
    this.users = users;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  public ExpenseDtos.Response create(ExpenseDtos.Request r, String email) {
    var user = users.findByEmailIgnoreCase(email).orElseThrow();
    var category =
        r.categoryId() == null
            ? null
            : categories
                .findByIdAndOwnerEmail(r.categoryId(), email)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    var expense =
        expenses.save(
            new Expense(
                r.title().trim(), r.description(), r.amount(), r.expenseDate(), user, category));
    eventPublisher.publish(
        new ExpenseCreatedEvent(
            expense.getId(),
            user.getId(),
            user.getEmail(),
            expense.getTitle(),
            expense.getAmount(),
            expense.getExpenseDate(),
            Instant.now()));
    return ExpenseDtos.Response.from(expense);
  }

  @Transactional
  public ExpenseDtos.Response update(Long id, ExpenseDtos.Request r, String email) {
    var expense =
        expenses
            .findByIdAndOwnerEmail(id, email)
            .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
    var category =
        r.categoryId() == null
            ? null
            : categories
                .findByIdAndOwnerEmail(r.categoryId(), email)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    expense.update(r.title().trim(), r.description(), r.amount(), r.expenseDate(), category);
    return ExpenseDtos.Response.from(expense);
  }

  @Transactional(readOnly = true)
  public ExpenseDtos.Response get(Long id, String email) {
    var expense =
        expenses
            .findByIdAndOwnerEmail(id, email)
            .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
    return ExpenseDtos.Response.from(expense);
  }

  @Transactional(readOnly = true)
  public List<ExpenseDtos.Response> list(String email) {
    return expenses.findAllByOwnerEmailOrderByExpenseDateDescIdDesc(email).stream()
        .map(ExpenseDtos.Response::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public ExpenseDtos.ExpensePage search(
      String email, LocalDate from, LocalDate to, Long categoryId, String q, int page, int size) {
    return search(email, from, to, categoryId, q, page, size, "expenseDate", "desc");
  }

  @Transactional(readOnly = true)
  public ExpenseDtos.ExpensePage search(
      String email,
      LocalDate from,
      LocalDate to,
      Long categoryId,
      String q,
      int page,
      int size,
      String sortBy,
      String sortDir) {
    if (page < 0) throw new IllegalArgumentException("Page must be >= 0");
    if (size < 1 || size > 100)
      throw new IllegalArgumentException("Size must be between 1 and 100");
    if (from != null && to != null && from.isAfter(to)) {
      throw new IllegalArgumentException("'from' date must be on or before 'to' date");
    }
    Specification<Expense> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          predicates.add(cb.equal(cb.lower(root.get("owner").get("email")), email.toLowerCase()));
          if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("expenseDate"), from));
          if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("expenseDate"), to));
          if (categoryId != null)
            predicates.add(cb.equal(root.get("category").get("id"), categoryId));
          if (q != null && !q.trim().isBlank()) {
            String pattern = "%" + q.trim().toLowerCase() + "%";
            Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
            Predicate descriptionMatch =
                cb.like(cb.lower(cb.coalesce(root.get("description"), "")), pattern);
            predicates.add(cb.or(titleMatch, descriptionMatch));
          }
          return cb.and(predicates.toArray(new Predicate[0]));
        };
    Sort sort = buildSort(sortBy, sortDir);
    Pageable pageable = PageRequest.of(page, size, sort);
    Page<Expense> result = expenses.findAll(spec, pageable);
    List<ExpenseDtos.Response> items =
        result.getContent().stream().map(ExpenseDtos.Response::from).toList();
    return new ExpenseDtos.ExpensePage(
        items,
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages());
  }

  private Sort buildSort(String sortBy, String sortDir) {
    String field = sortBy == null || sortBy.isBlank() ? "expenseDate" : sortBy.trim();
    String property =
        switch (field) {
          case "expenseDate", "date" -> "expenseDate";
          case "amount" -> "amount";
          case "title" -> "title";
          case "id" -> "id";
          default ->
              throw new IllegalArgumentException(
                  "sortBy must be one of: expenseDate, amount, title, id");
        };
    String direction = sortDir == null || sortDir.isBlank() ? "desc" : sortDir.trim().toLowerCase();
    Sort.Direction dir =
        switch (direction) {
          case "asc" -> Sort.Direction.ASC;
          case "desc" -> Sort.Direction.DESC;
          default -> throw new IllegalArgumentException("sortDir must be 'asc' or 'desc'");
        };
    Sort primary = Sort.by(new Sort.Order(dir, property));
    if (!"id".equals(property)) {
      primary = primary.and(Sort.by(Sort.Order.desc("id")));
    }
    return primary;
  }

  @Transactional
  public void delete(Long id, String email) {
    var expense =
        expenses
            .findByIdAndOwnerEmail(id, email)
            .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
    expenses.delete(expense);
  }

  @Transactional(readOnly = true)
  public ExpenseDtos.MonthlySummary summary(int year, int month, String email) {
    if (month < 1 || month > 12)
      throw new IllegalArgumentException("Month must be between 1 and 12");
    LocalDate start = LocalDate.of(year, month, 1);
    LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
    BigDecimal total = expenses.totalForPeriod(email, start, end);
    var categoryTotals =
        expenses.totalsByCategory(email, start, end).stream()
            .map(row -> new ExpenseDtos.CategoryTotal((String) row[0], (BigDecimal) row[1]))
            .toList();
    long count = expenses.countForPeriod(email, start, end);
    return new ExpenseDtos.MonthlySummary(year, month, total, count, categoryTotals);
  }

  @Transactional(readOnly = true)
  public ExpenseDtos.YearlySummary yearlySummary(int year, String email) {
    if (year < 1900 || year > 9999)
      throw new IllegalArgumentException("Year is out of supported range");
    LocalDate start = LocalDate.of(year, 1, 1);
    LocalDate end = LocalDate.of(year, 12, 31);
    BigDecimal total = expenses.totalForPeriod(email, start, end);
    long count = expenses.countForPeriod(email, start, end);

    Map<Integer, ExpenseDtos.MonthTotal> byMonth = new LinkedHashMap<>();
    for (int m = 1; m <= 12; m++) {
      byMonth.put(m, new ExpenseDtos.MonthTotal(m, BigDecimal.ZERO, 0));
    }
    for (Object[] row : expenses.totalsByMonth(email, year)) {
      int month = ((Number) row[0]).intValue();
      BigDecimal monthTotal = (BigDecimal) row[1];
      long monthCount = ((Number) row[2]).longValue();
      byMonth.put(month, new ExpenseDtos.MonthTotal(month, monthTotal, monthCount));
    }

    var categoryTotals =
        expenses.totalsByCategory(email, start, end).stream()
            .map(row -> new ExpenseDtos.CategoryTotal((String) row[0], (BigDecimal) row[1]))
            .toList();
    return new ExpenseDtos.YearlySummary(
        year, total, count, List.copyOf(byMonth.values()), categoryTotals);
  }
}
