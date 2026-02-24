package com.mypolicy.pipeline.processing.service;

import com.mypolicy.pipeline.common.util.DateParserUtil;
import com.mypolicy.pipeline.common.util.NumericParserUtil;
import com.mypolicy.pipeline.common.util.ValidationUtil;
import com.mypolicy.pipeline.metadata.model.FieldMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Transformation Service
 * Handles data type conversions and transformations based on field mappings
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataTransformationService {

  private final DateParserUtil dateParser;
  private final NumericParserUtil numericParser;
  private final ValidationUtil validator;

  /**
   * Transform raw Excel data to standardized format based on field mappings
   */
  public Map<String, Object> transformRecord(
      Map<String, Object> rawData,
      List<FieldMapping> mappings) {

    Map<String, Object> transformed = new HashMap<>();

    for (FieldMapping mapping : mappings) {
      String sourceField = mapping.getSourceField();
      String targetField = mapping.getTargetField();
      Object rawValue = rawData.get(sourceField);

      if (rawValue == null) {
        if (mapping.isRequired()) {
          log.warn("Required field '{}' is missing or null", sourceField);
        }
        transformed.put(targetField, mapping.getDefaultValue());
        continue;
      }

      // Apply transformation based on target field type
      Object transformedValue = applyTransformation(rawValue, mapping);
      transformed.put(targetField, transformedValue);
    }

    return transformed;
  }

  /**
   * Apply transformation rules to a single value
   */
  private Object applyTransformation(Object rawValue, FieldMapping mapping) {
    String transformRule = mapping.getTransformRule();
    
    if (transformRule == null || transformRule.isEmpty()) {
      return rawValue;
    }

    String strValue = rawValue.toString();

    return switch (transformRule.toLowerCase()) {
      case "uppercase" -> strValue.toUpperCase();
      case "lowercase" -> strValue.toLowerCase();
      case "trim" -> strValue.trim();
      case "normalize_mobile" -> validator.normalizeMobileNumber(strValue);
      case "normalize_pan" -> validator.normalizePanNumber(strValue);
      case "parse_date" -> dateParser.parseDate(strValue);
      case "parse_number" -> numericParser.parseBigDecimal(strValue);
      case "parse_integer" -> numericParser.parseInt(strValue);
      default -> rawValue;
    };
  }

  /**
   * Validate transformed record
   */
  public boolean validateRecord(Map<String, Object> record, List<FieldMapping> mappings) {
    for (FieldMapping mapping : mappings) {
      String targetField = mapping.getTargetField();
      Object value = record.get(targetField);

      // Check required fields
      if (mapping.isRequired() && (value == null || value.toString().trim().isEmpty())) {
        log.warn("Validation failed: required field '{}' is missing or empty", targetField);
        return false;
      }

      // Field-specific validations
      if (value != null && !value.toString().trim().isEmpty()) {
        if (targetField.equals("email") && !validator.isValidEmail(value.toString())) {
          log.warn("Validation failed: invalid email format for '{}'", value);
          return false;
        }
        if (targetField.equals("mobileNumber") && !validator.isValidMobileNumber(value.toString())) {
          log.warn("Validation failed: invalid mobile number format for '{}'", value);
          return false;
        }
        if (targetField.equals("panNumber") && !validator.isValidPanNumber(value.toString())) {
          log.warn("Validation failed: invalid PAN number format for '{}'", value);
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Enrich record with calculated/derived fields
   */
  public void enrichRecord(Map<String, Object> record) {
    // Calculate full name if first and last name exist
    if (record.containsKey("firstName") && record.containsKey("lastName")) {
      String fullName = record.get("firstName") + " " + record.get("lastName");
      record.put("fullName", fullName);
    }

    // Calculate age if date of birth exists
    if (record.containsKey("dateOfBirth") && record.get("dateOfBirth") instanceof LocalDate) {
      LocalDate dob = (LocalDate) record.get("dateOfBirth");
      int age = LocalDate.now().getYear() - dob.getYear();
      record.put("age", age);
    }

    // Calculate coverage ratio if premium and sum assured exist
    if (record.containsKey("premiumAmount") && record.containsKey("sumAssured")) {
      Object premiumObj = record.get("premiumAmount");
      Object sumAssuredObj = record.get("sumAssured");
      
      if (premiumObj instanceof BigDecimal && sumAssuredObj instanceof BigDecimal) {
        BigDecimal premium = (BigDecimal) premiumObj;
        BigDecimal sumAssured = (BigDecimal) sumAssuredObj;
        
        if (premium.compareTo(BigDecimal.ZERO) > 0) {
          BigDecimal ratio = sumAssured.divide(premium, 2, java.math.RoundingMode.HALF_UP);
          record.put("coverageRatio", ratio);
        }
      }
    }
  }
}
