package com.mypolicy.pipeline.matching.client;


import com.mypolicy.pipeline.matching.dto.CustomerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.Optional;

@FeignClient(name = "customer-service", url = "${customer.service.url:http://localhost:8081}")
public interface CustomerClient {

    // Level 1 Discovery
    @GetMapping("/api/v1/customers/search/pan/{pan}")
    Optional<CustomerDTO> findByPan(@PathVariable("pan") String pan);

    // Level 2 Discovery
    @GetMapping("/api/v1/customers/search/email/{email}")
    List<CustomerDTO> findByEmail(@PathVariable("email") String email);

    // Level 3 Discovery
    @GetMapping("/api/v1/customers/search/mobile/{mobile}")
    List<CustomerDTO> findByMobile(@PathVariable("mobile") String mobile);
}
