package com.smartstay.smartstay.responses.amenitity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmenityResponse {
    private String amenityId;
    private String amenityName;
    private Double amenityAmount;
    private boolean isProRate;
    List<CustomerResponse> assignedCustomers;
    List<CustomerResponse> unassignedCustomers;
}
