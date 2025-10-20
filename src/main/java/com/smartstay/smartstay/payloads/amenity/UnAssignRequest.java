package com.smartstay.smartstay.payloads.amenity;

import java.util.List;

public record UnAssignRequest(
        List<String> unassignedCustomers){
}
