package com.mypolicy.pipeline.matching.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDTO {
    private String customerId;    // The "Gold" ID we need for stitching
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String panNumber;     // Required for Level 1
    private String dateOfBirth;   // Required for Level 3
}
