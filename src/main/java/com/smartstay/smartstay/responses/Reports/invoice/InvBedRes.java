package com.smartstay.smartstay.responses.Reports.invoice;

import java.util.List;

public record InvBedRes(Integer bedId,
                        String bedName,
                        String tenantName,
                        String tenantCountryCode,
                        String tenantMobile,
                        String status,
                        String actualRent,
                        String pendingRent,
                        String ebAmount,
                        String pendingAdvance,
                        String pendingNonRefundable,
                        String totalPendingAmount,
                        String invoiceCount,
                        List<InvConsolidatedRes> invoices) {
}
