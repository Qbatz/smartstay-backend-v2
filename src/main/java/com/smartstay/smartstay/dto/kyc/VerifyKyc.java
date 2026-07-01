package com.smartstay.smartstay.dto.kyc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyKyc {
    @JsonProperty("id")
    private String id;
    @JsonProperty("updated_at")
    private String updatedAt;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("status")
    private String status;
    @JsonProperty("customer_identifier")
    private String customerIdentifier;
    @JsonProperty("actions")
    private List<VerifyKycActions> actions;
}
