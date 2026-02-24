package com.mypolicy.pipeline.common.audit;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for audit logs
 */
@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
  
  List<AuditLog> findByJobId(String jobId);
  
  List<AuditLog> findByOperation(String operation);
  
  List<AuditLog> findByPerformedBy(String performedBy);
  
  List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
  
  List<AuditLog> findByStatusAndTimestampAfter(String status, LocalDateTime timestamp);
}
