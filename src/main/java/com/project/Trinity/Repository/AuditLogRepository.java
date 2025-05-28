package com.project.Trinity.Repository;

import com.project.Trinity.Entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Son 10 işlemi zaman sırasına göre getir
    List<AuditLog> findTop10ByOrderByTimestampDesc();
}