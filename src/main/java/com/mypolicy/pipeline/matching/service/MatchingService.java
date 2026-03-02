package com.mypolicy.pipeline.matching.service;
import com.mypolicy.pipeline.matching.client.CustomerClient;
import com.mypolicy.pipeline.matching.client.PolicyClient;
import com.mypolicy.pipeline.matching.dto.*;
import com.mypolicy.pipeline.matching.engine.HierarchicalMatchingProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final CustomerClient customerClient;
    private final PolicyClient policyClient;
    private final HierarchicalMatchingProcessor matchingEngine;

    /**
     * The core flow: Fetch Candidates -> Apply Hierarchical Logic -> Stitch to Policy DB
     */
    public void processAndMatchPolicy(IncomingPolicyData incoming) {
        log.info("[Matching] Processing record for Policy: {}", incoming.getPolicyNumber());

        try {
            // 1. DISCOVERY: Query Customer Service via Feign
            // Using a Set to avoid duplicate candidates if they match multiple criteria
            Set<CustomerDTO> candidates = new HashSet<>();

            customerClient.findByPan(incoming.getPanNumber()).ifPresent(candidates::add);
            candidates.addAll(customerClient.findByEmail(incoming.getEmail()));
            candidates.addAll(customerClient.findByMobile(incoming.getMobileNumber()));

            // 2. LOGIC: Use your Java 21 Hierarchical Brain
            MatchResult result = matchingEngine.findBestMatch(incoming, new ArrayList<>(candidates));

            // 3. ACTION: Stitch or Flag
            if (result.isMatch()) {
                log.info("[Matching] SUCCESS: Found match via {} for Customer: {}",
                        result.getStatus(), result.getCustomerId());

                PolicyDTO policyToStitch = mapToPolicyDto(incoming, result.getCustomerId());
                policyClient.createStitchedPolicy(policyToStitch);
            } else {
                log.warn("[Matching] NO_MATCH: No candidate met the rigid logic for Policy: {}",
                        incoming.getPolicyNumber());
                handleNoMatch(incoming);
            }

        } catch (Exception e) {
            log.error("[Matching] CRITICAL ERROR: Failed to process policy {}",
                    incoming.getPolicyNumber(), e);
            // In a production pipeline, you might send this to a DLQ (Dead Letter Queue)
        }
    }

    private PolicyDTO mapToPolicyDto(IncomingPolicyData incoming, String customerId) {
        PolicyDTO dto = new PolicyDTO();
        dto.setPolicyNumber(incoming.getPolicyNumber());
        dto.setCustomerId(customerId); // THE STITCH
        dto.setInsurerId(incoming.getInsurerId());
        dto.setPolicyType(incoming.getPolicyType());
        dto.setStatus("STITCHED");
        return dto;
    }

    private void handleNoMatch(IncomingPolicyData incoming) {
        // TODO: Save to a 'manual_review' table in the local Pipeline database
        log.info("[Matching] Record sent to Manual Review Queue");
    }
}
