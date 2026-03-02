package com.mypolicy.pipeline.ingestion.dto;

import com.mypolicy.pipeline.ingestion.model.IngestionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal request for job status transition.
 * Enforces state machine: UPLOADED → PROCESSING → COMPLETED | FAILED
 */
@Data
@NoArgsConstructor  // <--- ADD THIS
@AllArgsConstructor // <--- AND THIS
public class StatusUpdateRequest {
  private IngestionStatus status;
  private String failureReason;
}