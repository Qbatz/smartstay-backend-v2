package com.smartstay.smartstay.dto.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaymentSession {
    @JsonProperty("sessionId")
    private String sessionId;
    @JsonProperty("amount")
    private String amount;
}
