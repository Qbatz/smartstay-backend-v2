package com.smartstay.smartstay.responses.Reports.invoice;

import com.smartstay.smartstay.responses.filters.EnumFilterRes;
import com.smartstay.smartstay.responses.filters.FloorsFilterRes;
import com.smartstay.smartstay.responses.filters.UserFilterRes;

import java.util.List;

public record InvConsolidatedFiltersRes(List<UserFilterRes> createdBy,
                                        List<EnumFilterRes> period,
                                        List<EnumFilterRes> paymentStatus,
                                        List<EnumFilterRes> invoiceMode,
                                        List<EnumFilterRes> invoiceType,
                                        List<FloorsFilterRes> floors,
                                        List<EnumFilterRes> groupBy) {
}
