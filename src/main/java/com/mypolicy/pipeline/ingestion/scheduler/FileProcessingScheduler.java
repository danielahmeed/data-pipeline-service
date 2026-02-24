package com.mypolicy.pipeline.ingestion.scheduler;

import com.mypolicy.pipeline.ingestion.model.IngestionJob;
import com.mypolicy.pipeline.ingestion.model.IngestionStatus;
import com.mypolicy.pipeline.ingestion.repository.IngestionJobRepository;
import com.mypolicy.pipeline.processing.service.ProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job to process uploaded files automatically
 * In production, this would be replaced with Kafka/message queue consumers
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileProcessingScheduler {

  private final IngestionJobRepository jobRepository;
  private final ProcessingService processingService;

  /**
   * Check for uploaded files and trigger processing
   * Runs every 30 seconds
   */
  @Scheduled(fixedDelay = 30000, initialDelay = 10000)
  public void processUploadedFiles() {
    log.debug("Checking for uploaded files to process...");

    List<IngestionJob> uploadedJobs = jobRepository.findByStatus(IngestionStatus.UPLOADED);

    if (uploadedJobs.isEmpty()) {
      log.debug("No uploaded files found");
      return;
    }

    log.info("Found {} uploaded files to process", uploadedJobs.size());

    for (IngestionJob job : uploadedJobs) {
      try {
        log.info("Processing job: {}", job.getJobId());
        
        // Note: In real implementation, policyType should be stored in job metadata
        // For now, we'll use a default or derive from insurer config
        String policyType = determineDefaultPolicyType(job.getInsurerId());
        
        processingService.processFile(
            job.getJobId(),
            job.getFilePath(),
            job.getInsurerId(),
            policyType
        );
      } catch (Exception e) {
        log.error("Failed to process job: {}", job.getJobId(), e);
        // Job status will be updated to FAILED by ProcessingService
      }
    }
  }

  /**
   * Retry failed jobs (with exponential backoff)
   * Runs every 5 minutes
   */
  @Scheduled(fixedDelay = 300000, initialDelay = 60000)
  public void retryFailedJobs() {
    log.debug("Checking for failed jobs to retry...");

    List<IngestionJob> failedJobs = jobRepository.findByStatus(IngestionStatus.FAILED);

    if (failedJobs.isEmpty()) {
      log.debug("No failed jobs found");
      return;
    }

    log.info("Found {} failed jobs", failedJobs.size());

    for (IngestionJob job : failedJobs) {
      // Only retry jobs that failed less than 3 times
      // In real implementation, add retry count field to IngestionJob
      LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
      
      if (job.getUpdatedAt().isAfter(oneHourAgo)) {
        log.info("Job {} failed recently, will retry later", job.getJobId());
        continue;
      }

      try {
        log.info("Retrying failed job: {}", job.getJobId());
        
        String policyType = determineDefaultPolicyType(job.getInsurerId());
        
        processingService.processFile(
            job.getJobId(),
            job.getFilePath(),
            job.getInsurerId(),
            policyType
        );
      } catch (Exception e) {
        log.error("Retry failed for job: {}", job.getJobId(), e);
      }
    }
  }

  /**
   * Determine default policy type based on insurer
   * This is a fallback - real implementation should store policy type in job metadata
   */
  private String determineDefaultPolicyType(String insurerId) {
    // Default mapping - could be enhanced with DB lookup
    if (insurerId.contains("LIFE")) {
      return "TERM_LIFE";
    } else if (insurerId.contains("HEALTH")) {
      return "HEALTH";
    } else if (insurerId.contains("MOTOR")) {
      return "MOTOR";
    }
    return "TERM_LIFE"; // Default fallback
  }
}
