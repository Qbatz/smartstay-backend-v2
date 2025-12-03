package com.smartstay.smartstay.responses.beds;

public record TenantInfo(String tenetId,
                         String tenantFirstName,
                         String tenantLastName,
                         String tenantFullName,
                         String profilePic,
                         String tenantInitials,
                         String joiningDate,
                         String bookingDate,
                         String mobile,
                         Double advance,
                         Double rentAmount,
                         Double lastInvoiceAmount,
                         String lastInvoiceNumber,
                         Integer totalInvoices,
                         String leavingDate,
                         String currentStatus,
                         String countryCode) {

}
