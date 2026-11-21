package com.harsh.expense.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ExpenseEventPublisher {
  private final ApplicationEventPublisher applicationEvents;

  public ExpenseEventPublisher(ApplicationEventPublisher applicationEvents) {
    this.applicationEvents = applicationEvents;
  }

  public void publish(ExpenseCreatedEvent event) {
    applicationEvents.publishEvent(event);
  }
}
