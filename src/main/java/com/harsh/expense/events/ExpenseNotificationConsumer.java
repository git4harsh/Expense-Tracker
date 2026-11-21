package com.harsh.expense.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ExpenseNotificationConsumer {
  private static final Logger log = LoggerFactory.getLogger(ExpenseNotificationConsumer.class);

  @KafkaListener(topics = "expense-created", groupId = "expense-notification-service")
  public void onExpenseCreated(ExpenseCreatedEvent event) {
    // Demonstration consumer: replace this log action with email/push delivery if required.
    log.info(
        "Notification event consumed. userId={}, expenseId={}, title={}, amount={}",
        event.userId(),
        event.expenseId(),
        event.title(),
        event.amount());
  }
}
