package com.smartstay.smartstay.payloads.customer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CustomersListRequest(
        String name,
        String status)
{
}
