/* Copyright (c) 2026 Harsh (git4harsh). All rights reserved. */
package com.harsh.expense.common;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityAuditRepository extends JpaRepository<SecurityAuditEvent, Long> {}
