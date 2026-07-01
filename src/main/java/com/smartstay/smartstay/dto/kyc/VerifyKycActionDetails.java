package com.smartstay.smartstay.dto.kyc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyKycActionDetails {
    @JsonProperty("aadhaar")
    private AadhaarDetails aadhaarDetails;
}
