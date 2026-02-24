package com.mypolicy.pipeline.common.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Utility class for parsing numeric values from various formats
 */
@Component
public class NumericParserUtil {

  /**
   * Parse BigDecimal from string, handling various formats
   * - Removes currency symbols (₹, $, etc.)
   * - Removes commas
   * - Handles percentage values
   * 
   * @param value String value to parse
   * @return BigDecimal if successful, null otherwise
   */
  public BigDecimal parseBigDecimal(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }

    try {
      // Remove currency symbols and commas
      String cleaned = value.trim()
          .replaceAll("[₹$€£¥,\\s]", "")
          .replace("%", "");

      return new BigDecimal(cleaned);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Parse integer from string
   */
  public Integer parseInt(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }

    try {
      String cleaned = value.trim().replaceAll("[,\\s]", "");
      return Integer.parseInt(cleaned);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Parse double from string
   */
  public Double parseDouble(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }

    try {
      String cleaned = value.trim().replaceAll("[,\\s]", "");
      return Double.parseDouble(cleaned);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
