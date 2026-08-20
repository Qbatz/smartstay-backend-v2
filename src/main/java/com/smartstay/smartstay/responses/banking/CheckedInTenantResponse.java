package com.smartstay.smartstay.responses.banking;

import com.smartstay.smartstay.responses.invoices.InvoicesList;

import java.util.List;

public record CheckedInTenantResponse(
        String firstName,
        String lastName,
        String fullName,
        String city,
        String state,
        String country,
        String mobile,
        String currentStatus,
        String emailId,
        String profilePic,
        String bedId,
        String floorId,
        String roomId,
        String customerId,
        String initials,
        String expectedJoiningDate,
        String actualJoining,
        String countryCode,
        String bookedAt,
        String bedName,
        String roomName,
        String floorName,
        List<InvoicesList> listInvoices) {
}
