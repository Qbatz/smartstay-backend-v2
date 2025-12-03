package com.smartstay.smartstay.payloads.recurring;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.smartstay.smartstay.annotations.BooleanDeserializer;

public record UpdateRecurring(
        @JsonDeserialize(using = BooleanDeserializer.class)
        Boolean status) {
}
