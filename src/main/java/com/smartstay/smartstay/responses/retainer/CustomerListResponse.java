package com.smartstay.smartstay.responses.retainer;

import java.util.List;

public record CustomerListResponse(String hostelId,
                                   List<CustomersList> customersLists) {
}
