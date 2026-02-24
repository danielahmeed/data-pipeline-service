package com.mypolicy.pipeline.common.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for date parsing with multiple formats support
 */
@Component
public class DateParserUtil {

  private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
      DateTimeFormatter.ofPattern("yyyy-MM-dd"),
      DateTimeFormatter.ofPattern("dd/MM/yyyy"),
      DateTimeFormatter.ofPattern("MM/dd/yyyy"),
      DateTimeFormatter.ofPattern("dd-MM-yyyy"),
      DateTimeFormatter.ofPattern("yyyy/MM/dd"),
      DateTimeFormatter.ISO_LOCAL_DATE);

  /**
   * Parse date string with multiple format attempts
   * 
   * @param dateStr Date string to parse
   * @return LocalDate if successful, null otherwise
   */
  public LocalDate parseDate(String dateStr) {
    if (dateStr == null || dateStr.trim().isEmpty()) {
      return null;
    }

    for (DateTimeFormatter formatter : DATE_FORMATTERS) {
      try {
        return LocalDate.parse(dateStr.trim(), formatter);
      } catch (DateTimeParseException e) {
        // Try next format
      }
    }

    // If all formats fail, return null
    return null;
  }

  /**
   * Parse date with specific format
   */
  public LocalDate parseDate(String dateStr, String format) {
    if (dateStr == null || dateStr.trim().isEmpty()) {
      return null;
    }

    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
      return LocalDate.parse(dateStr.trim(), formatter);
    } catch (DateTimeParseException e) {
      return null;
    }
  }
}
