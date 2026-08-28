package com.smartstay.smartstay.dto.vendor;

import com.smartstay.smartstay.ennum.VendorPaymentStatus;

import java.util.Date;
import java.util.List;

public record VendorFilters(
        String name,
        Integer categoryId,
        List<VendorPaymentStatus> paymentStatuses,
        List<String> createdBy,
        Date createdFrom,
        Date createdTo,
        Long subCategoryId,
        Double minBalance,
        Double maxBalance) {
}
