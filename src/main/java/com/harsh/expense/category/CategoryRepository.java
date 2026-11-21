/* Copyright (c) 2026 Harsh (git4harsh). All rights reserved. */
package com.harsh.expense.category;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  List<Category> findAllByOwnerEmailOrderByName(String email);

  Optional<Category> findByIdAndOwnerEmail(Long id, String email);

  boolean existsByOwnerIdAndNameIgnoreCase(Long ownerId, String name);
}
