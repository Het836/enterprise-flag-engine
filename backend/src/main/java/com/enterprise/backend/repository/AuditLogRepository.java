package com.enterprise.backend.repository;

import com.enterprise.backend.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Allows the admin panel to quickly look up the history trail for any specific flag
    List<AuditLog> findByFlagKeyOrderByIdDesc(String flagKey);
}
