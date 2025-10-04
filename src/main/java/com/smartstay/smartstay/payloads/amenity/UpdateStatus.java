package com.smartstay.smartstay.payloads.amenity;

import java.util.List;

public record UpdateStatus(
        List<String> assignedCustomers,
        List<String> unassignedCustomers
) {
}
