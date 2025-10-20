package com.smartstay.smartstay.payloads.amenity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public record AssignAmenity(List<String> assignedCustomers,
        List<String> unassignedCustomers) {

}
