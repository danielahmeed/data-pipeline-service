package com.mypolicy.pipeline.processing.service;

import com.mypolicy.pipeline.metadata.model.FieldMapping;
import com.mypolicy.pipeline.metadata.model.InsurerConfiguration;
import com.mypolicy.pipeline.metadata.service.MetadataService;
import com.mypolicy.pipeline.ingestion.service.IngestionService;
import com.mypolicy.pipeline.ingestion.dto.StatusUpdateRequest;
import com.mypolicy.pipeline.ingestion.model.IngestionStatus;
import com.mypolicy.pipeline.matching.service.MatchingService;
import com.mypolicy.pipeline.matching.dto.IncomingPolicyData; // New Import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessingService {

  private final MetadataService metadataService;
  private final IngestionService ingestionService;
  private final MatchingService matchingService;

  public void processFile(String jobId, String filePath, String insurerId, String policyType) {
    log.info("[Processing] Starting file processing: jobId={}, insurerId={}", jobId, insurerId);

    try {
      // FIX: Using setters instead of the 2-arg constructor
      StatusUpdateRequest processingStatus = new StatusUpdateRequest();
      processingStatus.setStatus(IngestionStatus.PROCESSING);
      ingestionService.updateStatus(jobId, processingStatus);

      // 1. Fetch Metadata Rules
      InsurerConfiguration config = metadataService.getConfiguration(insurerId);
      List<FieldMapping> mappings = metadataService.getMappingsForPolicyType(insurerId, policyType);

      // 2. Read Excel & Parse to POJOs
      List<IncomingPolicyData> recordsToMatch = parseExcelToPojos(filePath, mappings, insurerId, policyType, jobId);

      log.info("[Processing] Parsed {} records. Sending to Hierarchical Matching Engine.", recordsToMatch.size());

      // 3. Hand-off to Matching Service
      for (IncomingPolicyData record : recordsToMatch) {
        try {
          matchingService.processAndMatchPolicy(record);
        } catch (Exception e) {
          log.error("[Processing] Match failed for policy: {}", record.getPolicyNumber(), e);
        }
      }

      // FIX: Using setters for COMPLETED status
      StatusUpdateRequest completedStatus = new StatusUpdateRequest();
      completedStatus.setStatus(IngestionStatus.COMPLETED);
      ingestionService.updateStatus(jobId, completedStatus);

    } catch (Exception e) {
      log.error("[Processing] Fatal error in jobId={}", jobId, e);

      // FIX: Passing both arguments for the FAILED case (Status and Reason)
      StatusUpdateRequest failedStatus = new StatusUpdateRequest();
      failedStatus.setStatus(IngestionStatus.FAILED);
      failedStatus.setFailureReason(e.getMessage());
      ingestionService.updateStatus(jobId, failedStatus);

      throw new RuntimeException("Processing failed", e);
    }
  }

  private List<IncomingPolicyData> parseExcelToPojos(String path, List<FieldMapping> mappings,
                                                     String insurerId, String policyType, String jobId) throws IOException {

    List<IncomingPolicyData> records = new ArrayList<>();

    try (InputStream is = new FileInputStream(path); Workbook workbook = WorkbookFactory.create(is)) {
      Sheet sheet = workbook.getSheetAt(0);
      Row headerRow = sheet.getRow(0);
      Map<String, Integer> colMap = new HashMap<>();

      for (Cell cell : headerRow) {
        colMap.put(cell.getStringCellValue(), cell.getColumnIndex());
      }

      ingestionService.setTotalRecords(jobId, sheet.getLastRowNum());

      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) continue;

        IncomingPolicyData data = new IncomingPolicyData();
        data.setInsurerId(insurerId);
        data.setPolicyType(policyType);

        for (FieldMapping m : mappings) {
          Integer idx = colMap.get(m.getSourceField());
          if (idx != null) {
            String val = String.valueOf(getCellValue(row.getCell(idx)));
            mapField(data, m.getTargetField(), val);
          }
        }
        records.add(data);
      }
    }
    return records;
  }

  /**
   * Bridges Excel column values to the IncomingPolicyData POJO fields.
   */
  private void mapField(IncomingPolicyData data, String targetField, String value) {
    if (value == null || value.equalsIgnoreCase("null")) return;

    switch (targetField) {
      case "policyNumber" -> data.setPolicyNumber(value);
      case "panNumber"    -> data.setPanNumber(value);
      case "email"        -> data.setEmail(value);
      case "mobileNumber" -> data.setMobileNumber(value);
      case "dateOfBirth"  -> data.setDateOfBirth(value);
      case "firstName"    -> data.setFirstName(value);
      case "lastName"     -> data.setLastName(value);
    }
  }

  private Object getCellValue(Cell cell) {
    if (cell == null) return null;
    return switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue();
      case NUMERIC -> DateUtil.isCellDateFormatted(cell) ? cell.getDateCellValue() : cell.getNumericCellValue();
      case BOOLEAN -> cell.getBooleanCellValue();
      default -> null;
    };
  }
}