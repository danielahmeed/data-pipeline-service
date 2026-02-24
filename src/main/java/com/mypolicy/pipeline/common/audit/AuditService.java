package com.mypolicy.pipeline.common.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for creating audit logs
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

  private final AuditLogRepository auditLogRepository;

  /**
   * Log successful operation
   */
  public void logSuccess(String operation, String entityType, String entityId, 
                         String performedBy, Long durationMs) {
    AuditLog auditLog = AuditLog.builder()
        .operation(operation)
        .status("SUCCESS")
        .entityType(entityType)
        .entityId(entityId)
        .performedBy(performedBy)
        .durationMs(durationMs)
        .timestamp(LocalDateTime.now())
        .build();

    auditLogRepository.save(auditLog);
    log.debug("Audit log created: {} - {}", operation, entityId);
  }

  /**
   * Log failed operation
   */
  public void logFailure(String operation, String entityType, String entityId, 
                         String performedBy, String errorMessage, Long durationMs) {
    AuditLog auditLog = AuditLog.builder()
        .operation(operation)
        .status("FAILURE")
        .entityType(entityType)
        .entityId(entityId)
        .performedBy(performedBy)
        .errorMessage(errorMessage)
        .durationMs(durationMs)
        .timestamp(LocalDateTime.now())
        .build();

    auditLogRepository.save(auditLog);
    log.warn("Audit log created: {} - {} - FAILED: {}", operation, entityId, errorMessage);
  }

  /**
   * Log operation with custom metadata
   */
  public void logWithMetadata(String operation, String entityType, String entityId, 
                              String performedBy, Map<String, Object> metadata, 
                              boolean success, String errorMessage, Long durationMs) {
    AuditLog auditLog = AuditLog.builder()
        .operation(operation)
        .status(success ? "SUCCESS" : "FAILURE")
        .entityType(entityType)
        .entityId(entityId)
        .performedBy(performedBy)
        .metadata(metadata != null ? metadata : new HashMap<>())
        .errorMessage(errorMessage)
        .durationMs(durationMs)
        .timestamp(LocalDateTime.now())
        .build();

    auditLogRepository.save(auditLog);
  }

  /**
   * Log job-specific operation
   */
  public void logJobOperation(String jobId, String operation, String performedBy, 
                               boolean success, String errorMessage) {
    AuditLog auditLog = AuditLog.builder()
        .jobId(jobId)
        .operation(operation)
        .status(success ? "SUCCESS" : "FAILURE")
        .entityType("JOB")
        .entityId(jobId)
        .performedBy(performedBy)
        .errorMessage(errorMessage)
        .timestamp(LocalDateTime.now())
        .build();

    auditLogRepository.save(auditLog);
  }
}
