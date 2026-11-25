package com.smartstay.smartstay.responses.Reports.invoice;

import com.smartstay.smartstay.responses.filters.EnumFilterResponse;
import com.smartstay.smartstay.responses.filters.FloorsFilterResponse;
import com.smartstay.smartstay.responses.filters.UserFilterResponse;

import java.util.List;

public record InvoiceConsolidatedFiltersResponse(List<UserFilterResponse> createdBy,
                                                 List<EnumFilterResponse> period,
                                                 List<EnumFilterResponse> paymentStatus,
                                                 List<EnumFilterResponse> invoiceMode,
                                                 List<EnumFilterResponse> invoiceType,
                                                 List<FloorsFilterResponse> floors,
                                                 List<EnumFilterResponse> groupBy) {
}
