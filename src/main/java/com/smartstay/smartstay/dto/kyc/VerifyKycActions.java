package com.smartstay.smartstay.dto.kyc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyKycActions {
    @JsonProperty("id")
    private String id;
    @JsonProperty("action_ref")
    private String actionReference;
    @JsonProperty("type")
    private String type;
    @JsonProperty("status")
    private String status;
    @JsonProperty("execution_request_id")
    private String executionRequestId;
    @JsonProperty("completed_at")
    private String completedAt;
    @JsonProperty("details")
    private VerifyKycActionDetails details;

}
