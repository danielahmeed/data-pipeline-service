package com.mypolicy.pipeline.matching.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PolicyDTO {
    private String policyNumber;
    private String customerId;    // THE STITCH: The link to the Master Customer
    private String insurerId;
    private String policyType;
    private BigDecimal premiumAmount;
    private String expiryDate;
    private String status;        // Usually set to "ACTIVE" or "STITCHED"
}