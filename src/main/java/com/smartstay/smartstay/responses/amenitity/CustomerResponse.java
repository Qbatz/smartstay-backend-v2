package com.smartstay.smartstay.responses.amenitity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponse {
    private String customerId;
    private String customerName;
    private String initials;
    private String profilePic;
    private boolean canAssign;
}

