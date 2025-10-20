package com.smartstay.smartstay.payloads.amenity;

import java.util.List;

public record AssignRequest(
        List<String> assignedCustomers
) {
}
