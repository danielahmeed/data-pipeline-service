package com.mypolicy.pipeline.matching.client;


import com.mypolicy.pipeline.matching.dto.PolicyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "policy-service", url = "${policy.service.url:http://localhost:8082}")
public interface PolicyClient {

    /**
     * Sends the stitched policy record to the Policy Service.
     * This effectively "completes" the pipeline for a single record.
     */
    @PostMapping("/api/v1/policies")
    void createStitchedPolicy(@RequestBody PolicyDTO policy);
}