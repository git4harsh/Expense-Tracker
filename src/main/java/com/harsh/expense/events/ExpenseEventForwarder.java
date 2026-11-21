/* Copyright (c) 2026 Harsh (git4harsh). All rights reserved. */
package com.harsh.expense.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ExpenseEventForwarder {
  private static final Logger log = LoggerFactory.getLogger(ExpenseEventForwarder.class);
  private static final String TOPIC = "expense-created";
  private final KafkaTemplate<String, ExpenseCreatedEvent> kafka;

  public ExpenseEventForwarder(KafkaTemplate<String, ExpenseCreatedEvent> kafka) {
    this.kafka = kafka;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void afterExpenseCommit(ExpenseCreatedEvent event) {
    kafka
        .send(TOPIC, String.valueOf(event.userId()), event)
        .whenComplete(
            (result, error) -> {
              if (error != null) {
                log.error(
                    "Failed to publish expense-created event. expenseId={}",
                    event.expenseId(),
                    error);
              } else {
                log.info(
                    "Published expense-created event. expenseId={}, partition={}, offset={}",
                    event.expenseId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
              }
            });
  }
}
