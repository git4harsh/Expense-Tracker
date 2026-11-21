package com.harsh.expense.events;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
  @Bean
  NewTopic expenseCreatedTopic() {
    return TopicBuilder.name("expense-created").partitions(1).replicas(1).build();
  }
}
