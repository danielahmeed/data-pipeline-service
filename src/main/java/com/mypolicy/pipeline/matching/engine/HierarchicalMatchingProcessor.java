package com.mypolicy.pipeline.matching.engine;


import com.mypolicy.pipeline.matching.dto.CustomerDTO;
import com.mypolicy.pipeline.matching.dto.IncomingPolicyData;
import com.mypolicy.pipeline.matching.dto.MatchResult;
import com.mypolicy.pipeline.matching.engine.FuzzyMatcher;


import java.util.List;

public class HierarchicalMatchingProcessor {

    private final FuzzyMatcher fuzzyMatcher = new FuzzyMatcher();

    public MatchResult findBestMatch(IncomingPolicyData incoming, List<CustomerDTO> candidates) {
        // Defensive check to avoid NullPointerExceptions
        if (incoming == null || candidates == null || candidates.isEmpty()) {
            return new MatchResult(null, "NO_MATCH_FOUND");
        }

        for (CustomerDTO master : candidates) {

            // 1. LEVEL 1: PAN Only (Absolute Priority)
            if (isMatch(incoming.getPanNumber(), master.getPanNumber())) {
                return new MatchResult(master.getCustomerId(), "SUCCESS_MATCH_PAN");
            }

            // 2. LEVEL 2: Email + Mobile (Composite Match)
            if (isMatch(incoming.getEmail(), master.getEmail()) &&
                    isMatch(incoming.getMobileNumber(), master.getMobileNumber())) {
                return new MatchResult(master.getCustomerId(), "SUCCESS_MATCH_EMAIL_MOBILE");
            }

            // 3. LEVEL 3: Mobile + DOB + Fuzzy Name (Validation Cluster)
            if (isMatch(incoming.getMobileNumber(), master.getMobileNumber()) &&
                    isMatch(incoming.getDateOfBirth(), master.getDateOfBirth()) &&
                    fuzzyMatcher.isSimilar(incoming, master)) {

                return new MatchResult(master.getCustomerId(), "SUCCESS_MATCH_MOBILE_DOB_FUZZY");
            }
        }

        return new MatchResult(null, "NO_MATCH_FOUND");
    }

    private boolean isMatch(String val1, String val2) {
        return val1 != null && val1.trim().equalsIgnoreCase(val2 != null ? val2.trim() : null);
    }
}
