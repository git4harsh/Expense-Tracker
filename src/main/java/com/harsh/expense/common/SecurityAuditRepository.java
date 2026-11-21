package com.harsh.expense.common;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityAuditRepository extends JpaRepository<SecurityAuditEvent, Long> {}
