package com.smartstay.smartstay.responses.customer;

/**
 * Response for the Create Customer API. Carries the success message plus the generated customerId,
 * which is populated only after the record is successfully persisted.
 */
public record CreateCustomerResponse(
        String message,
        String customerId) {
}
