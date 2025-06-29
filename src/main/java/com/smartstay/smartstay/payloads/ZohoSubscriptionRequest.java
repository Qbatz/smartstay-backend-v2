package com.smartstay.smartstay.payloads;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ZohoSubscriptionRequest {

    private Plan plan;
    private Customer customer;
    private String starts_at;
    private String notes;

    @Data
    public static class Plan {
        private String plan_code;
    }

    @Data
    public static class Customer {
        private String display_name;
        private String first_name;
        private String last_name;
        private String email;
        private String mobile;
    }
}

