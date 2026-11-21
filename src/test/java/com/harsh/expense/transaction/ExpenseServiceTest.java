package com.harsh.expense.transaction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.harsh.expense.category.CategoryRepository;
import com.harsh.expense.events.ExpenseEventPublisher;
import com.harsh.expense.user.AppUser;
import com.harsh.expense.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {
  @Mock ExpenseRepository expenses;
  @Mock CategoryRepository categories;
  @Mock UserRepository users;
  @Mock ExpenseEventPublisher eventPublisher;
  @InjectMocks ExpenseService service;

  @Test
  void createsExpenseForAuthenticatedOwnerAndPublishesEvent() {
    AppUser owner = new AppUser("owner@example.com", "Owner", "hash");
    when(users.findByEmailIgnoreCase("owner@example.com")).thenReturn(Optional.of(owner));
    when(expenses.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var request =
        new ExpenseDtos.Request(
            "Coffee", "Morning coffee", new BigDecimal("4.50"), LocalDate.of(2026, 11, 21), null);

    var result = service.create(request, "owner@example.com");

    assertEquals("Coffee", result.title());
    assertEquals(new BigDecimal("4.50"), result.amount());
    verify(eventPublisher).publish(any());
    verify(expenses).save(any(Expense.class));
  }

  @Test
  void rejectsCategoryThatDoesNotBelongToAuthenticatedOwner() {
    AppUser owner = new AppUser("owner@example.com", "Owner", "hash");
    when(users.findByEmailIgnoreCase("owner@example.com")).thenReturn(Optional.of(owner));
    when(categories.findByIdAndOwnerEmail(99L, "owner@example.com")).thenReturn(Optional.empty());

    var request =
        new ExpenseDtos.Request(
            "Coffee", null, new BigDecimal("4.50"), LocalDate.of(2026, 11, 21), 99L);

    assertThrows(
        IllegalArgumentException.class, () -> service.create(request, "owner@example.com"));
    verify(expenses, never()).save(any(Expense.class));
    verify(eventPublisher, never()).publish(any());
  }

  @Test
  void rejectsInvalidMonthInMonthlyReport() {
    assertThrows(
        IllegalArgumentException.class, () -> service.summary(2026, 13, "owner@example.com"));
  }

  @Test
  void rejectsMissingExpenseWhenFetchingById() {
    when(expenses.findByIdAndOwnerEmail(42L, "owner@example.com")).thenReturn(Optional.empty());
    assertThrows(IllegalArgumentException.class, () -> service.get(42L, "owner@example.com"));
  }

  @Test
  void rejectsInvalidExpenseSearchParameters() {
    assertThrows(
        IllegalArgumentException.class,
        () -> service.search("owner@example.com", null, null, null, null, -1, 20));
    assertThrows(
        IllegalArgumentException.class,
        () -> service.search("owner@example.com", null, null, null, null, 0, 0));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            service.search(
                "owner@example.com",
                LocalDate.of(2026, 11, 30),
                LocalDate.of(2026, 11, 1),
                null,
                null,
                0,
                20));
  }

  @Test
  void rejectsInvalidYearInYearlyReport() {
    assertThrows(
        IllegalArgumentException.class, () -> service.yearlySummary(1800, "owner@example.com"));
  }
}
