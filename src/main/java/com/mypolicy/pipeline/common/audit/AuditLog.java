package com.mypolicy.pipeline.common.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Audit log for tracking all important operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
public class AuditLog {

  @Id
  private String id;

  private String jobId;
  private String operation; // UPLOAD, PROCESS, MATCH, etc.
  private String status; // SUCCESS, FAILURE
  private String performedBy;
  private String entityType; // JOB, POLICY, CUSTOMER
  private String entityId;
  private Map<String, Object> metadata;
  private String errorMessage;
  private LocalDateTime timestamp;
  private Long durationMs;
}
