package com.mypolicy.pipeline.matching.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchResult {
    private String customerId; // The ID of the matched customer (null if no match)
    private String status;     // e.g., "SUCCESS_MATCH_PAN", "NO_MATCH_FOUND"

    // Helper method for the Service layer
    public boolean isMatch() {
        return customerId != null;
    }
}