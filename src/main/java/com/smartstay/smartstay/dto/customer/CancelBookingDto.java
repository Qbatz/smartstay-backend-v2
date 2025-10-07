package com.smartstay.smartstay.dto.customer;

public record CancelBookingDto(String reason,
                               String customerId,
                               Double amount,
                               String invoiceId,
                               String bankId,
                               String referenceNumber) {
}
