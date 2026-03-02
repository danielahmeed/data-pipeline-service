package com.mypolicy.pipeline.matching.dto;


import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents the raw data arriving from the Data Pipeline.
 * Used for R&D to test matching logic against CustomerDTO.
 */
@Data
public class IncomingPolicyData {
    // Identity Fields (Used for Matching)
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String panNumber;
    private String dateOfBirth;

    // Policy Fields (Used for Stitching)
    private String policyNumber;
    private String insurerId;
    private String policyType;
    private String planName;
    private BigDecimal premiumAmount;
    private BigDecimal sumAssured;
    private LocalDate startDate;
}