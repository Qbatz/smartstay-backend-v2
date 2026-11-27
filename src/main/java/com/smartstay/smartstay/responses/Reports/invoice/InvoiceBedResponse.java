package com.smartstay.smartstay.responses.Reports.invoice;

import java.util.List;

public record InvoiceBedResponse(Integer bedId,
                                 String bedName,
                                 String tenantName,
                                 String tenantCountryCode,
                                 String tenantMobile,
                                 String tenantProfilePic,
                                 String tenantInitials,
                                 String status,
                                 String actualRent,
                                 String pendingRent,
                                 String ebAmount,
                                 String pendingAdvance,
                                 String pendingNonRefundable,
                                 String totalPendingAmount,
                                 String invoiceCount,
                                 List<InvoiceConsolidatedResponse> invoices) {
}
