package com.mypolicy.pipeline.matching.engine;


import com.mypolicy.pipeline.matching.dto.CustomerDTO;
import com.mypolicy.pipeline.matching.dto.IncomingPolicyData;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for string similarity calculations.
 * Encapsulates the Levenshtein logic from the original service.
 */
public class FuzzyMatcher {

    private static final Logger log = LoggerFactory.getLogger(FuzzyMatcher.class);
    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

    // Max edit distance for a successful fuzzy match
    private static final int SIMILARITY_THRESHOLD = 3;

    /**
     * Compares the name of an incoming record against a master record.
     * Normalizes both strings to lowercase for a fair comparison.
     */
    public boolean isSimilar(IncomingPolicyData incoming, CustomerDTO master) {
        if (incoming.getFirstName() == null || master.getFirstName() == null) {
            return false;
        }

        String fullNameIncoming = (incoming.getFirstName() + " " + incoming.getLastName()).toLowerCase().trim();
        String fullNameMaster = (master.getFirstName() + " " + master.getLastName()).toLowerCase().trim();

        int distance = levenshteinDistance.apply(fullNameIncoming, fullNameMaster);

        log.debug("[Matching] Fuzzy distance between '{}' and '{}' is {}",
                fullNameIncoming, fullNameMaster, distance);

        // Returns true if the names are within the allowed "error" distance
        return distance <= SIMILARITY_THRESHOLD;
    }
}

