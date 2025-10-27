package com.smartstay.smartstay.dto.kyc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestKyc {
    @JsonProperty("id")
    String id;
    @JsonProperty("created_at")
    String createdAt;
    @JsonProperty("status")
    String status;
    @JsonProperty("customer_identifier")
    String customerIdentifier;
    @JsonProperty("reference_id")
    String referenceId;
    @JsonProperty("transaction_id")
    String transactionId;
    @JsonProperty("customer_name")
    String customerName;
    @JsonProperty("expire_in_days")
    int expiresInDays;
    @JsonProperty("reminder_registered")
    boolean reminderRegistered;
    @JsonProperty("workflow_name")
    String workflowName;
    @JsonProperty("auto_approved")
    boolean autoApproved;
    @JsonProperty("template_id")
    String templateId;
    @JsonProperty("access_token")
    AccessToken accessToken;

    public static class AccessToken {
        @JsonProperty("entity_id")
        String entityId;
        @JsonProperty("id")
        String id;
        @JsonProperty("valid_till")
        String validTill;
        @JsonProperty("created_at")
        String createdAt;
    }
}
