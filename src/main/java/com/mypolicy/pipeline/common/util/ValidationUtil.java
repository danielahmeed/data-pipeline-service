package com.mypolicy.pipeline.common.util;

import org.springframework.stereotype.Component;

/**
 * Utility class for string validation and cleaning
 */
@Component
public class ValidationUtil {

  /**
   * Validate email format
   */
  public boolean isValidEmail(String email) {
    if (email == null || email.trim().isEmpty()) {
      return false;
    }
    String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    return email.matches(emailRegex);
  }

  /**
   * Validate Indian mobile number (10 digits)
   */
  public boolean isValidMobileNumber(String mobile) {
    if (mobile == null || mobile.trim().isEmpty()) {
      return false;
    }
    // Remove spaces and hyphens
    String cleaned = mobile.replaceAll("[\\s-]", "");
    // Check if it's 10 digits
    return cleaned.matches("^[6-9]\\d{9}$");
  }

  /**
   * Validate PAN number (Indian format)
   */
  public boolean isValidPanNumber(String pan) {
    if (pan == null || pan.trim().isEmpty()) {
      return false;
    }
    // PAN format: 5 letters, 4 digits, 1 letter
    return pan.matches("^[A-Z]{5}[0-9]{4}[A-Z]$");
  }

  /**
   * Clean and normalize mobile number
   */
  public String normalizeMobileNumber(String mobile) {
    if (mobile == null) {
      return null;
    }
    // Remove all non-digit characters
    String cleaned = mobile.replaceAll("\\D", "");
    // Remove country code if present (91)
    if (cleaned.startsWith("91") && cleaned.length() == 12) {
      cleaned = cleaned.substring(2);
    }
    return cleaned;
  }

  /**
   * Clean and normalize PAN number
   */
  public String normalizePanNumber(String pan) {
    if (pan == null) {
      return null;
    }
    return pan.trim().toUpperCase();
  }

  /**
   * Check if string is null or empty
   */
  public boolean isNullOrEmpty(String str) {
    return str == null || str.trim().isEmpty();
  }

  /**
   * Clean whitespace from string
   */
  public String cleanString(String str) {
    if (str == null) {
      return null;
    }
    return str.trim().replaceAll("\\s+", " ");
  }
}
