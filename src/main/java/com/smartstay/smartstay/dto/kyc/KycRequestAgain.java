package com.smartstay.smartstay.dto.kyc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KycRequestAgain {

    @JsonProperty("response")
    private Response response;
    @JsonProperty("session")
    private Session session;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        @JsonProperty("entity_id")
        private String entityId;
        @JsonProperty("id")
        private String id;
        @JsonProperty("valid_till")
        private String validTill;
        @JsonProperty("created_at")
        private String createdAt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Session {
        @JsonProperty("sid")
        private String sId;
        @JsonProperty("is_logged_in")
        private Boolean isLoggedIn;
    }
}
