/* Copyright (c) 2026 Harsh (git4harsh). All rights reserved. */
package com.harsh.expense.category;

import com.harsh.expense.user.AppUser;
import jakarta.persistence.*;

@Entity
@Table(
    name = "categories",
    uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "name"}))
public class Category {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String name;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  private AppUser owner;

  protected Category() {}

  public Category(String name, AppUser owner) {
    this.name = name;
    this.owner = owner;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public AppUser getOwner() {
    return owner;
  }

  public void setName(String name) {
    this.name = name;
  }
}
