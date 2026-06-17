package com.smartstay.smartstay.Wrappers.vendor;

import com.smartstay.smartstay.dao.VendorV1;
import com.smartstay.smartstay.dto.vendor.VendorExpenseAggregate;
import com.smartstay.smartstay.responses.vendor.VendorHeaderAdditionalFields;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;
import com.smartstay.smartstay.util.columnOptions.VendorColumnUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Maps a {@link VendorV1} into an ordered list of cell values, one per visible/configured column,
 * followed by a trailing {@link VendorHeaderAdditionalFields} object carrying the vendor id.
 *
 * <p>Category names and per-vendor expense roll-ups are passed in as pre-built maps so the listing
 * service can resolve them with a couple of bulk queries instead of per-row lookups (no N+1).
 */
public class VendorTableMapper implements Function<VendorV1, List<Object>> {

    private final List<String> columns;
    private final Map<Integer, String> categoryNamesById;
    private final Map<String, VendorExpenseAggregate> aggregatesByVendorId;

    public VendorTableMapper(List<String> columns,
                             Map<Integer, String> categoryNamesById,
                             Map<String, VendorExpenseAggregate> aggregatesByVendorId) {
        this.columns = columns != null ? columns : List.of();
        this.categoryNamesById = categoryNamesById != null ? categoryNamesById : Collections.emptyMap();
        this.aggregatesByVendorId = aggregatesByVendorId != null ? aggregatesByVendorId : Collections.emptyMap();
    }

    @Override
    public List<Object> apply(VendorV1 vendor) {
        VendorExpenseAggregate aggregate = aggregatesByVendorId.get(String.valueOf(vendor.getVendorId()));
        List<Object> columnItems = new ArrayList<>();
        columns.forEach(column -> columnItems.add(getColumnItem(vendor, aggregate, column)));
        columnItems.add(new VendorHeaderAdditionalFields(String.valueOf(vendor.getVendorId())));
        return columnItems;
    }

    private String getColumnItem(VendorV1 vendor, VendorExpenseAggregate aggregate, String column) {
        if (column.equalsIgnoreCase(VendorColumnUtils.PROFILE_PIC)) {
            if (vendor.getProfilePic() != null && !vendor.getProfilePic().trim().isEmpty()) {
                return vendor.getProfilePic();
            }
            return NameUtils.getInitials(vendor.getFirstName(), vendor.getLastName());
        }
        if (column.equalsIgnoreCase(VendorColumnUtils.FULL_NAME)) {
            return NameUtils.getFullName(vendor.getFirstName(), vendor.getLastName());
        }
        if (column.equalsIgnoreCase(VendorColumnUtils.JOINING_DATE)) {
            return vendor.getCreatedAt() != null ? Utils.dateToString(vendor.getCreatedAt()) : "NA";
        }
        if (column.equalsIgnoreCase(VendorColumnUtils.MOBILE_NUMBER)) {
            if (vendor.getMobile() == null || vendor.getMobile().trim().isEmpty()) {
                return "-";
            }
            String countryCode = vendor.getCountryCode() != null && !vendor.getCountryCode().trim().isEmpty()
                    ? vendor.getCountryCode().trim() : "91";
            return "+" + countryCode + " " + vendor.getMobile();
        }
        if (column.equalsIgnoreCase(VendorColumnUtils.EMAIL_ID)) {
            return vendor.getEmailId() != null && !vendor.getEmailId().trim().isEmpty() ? vendor.getEmailId() : "-";
        }
        if (column.equalsIgnoreCase(VendorColumnUtils.VENDOR_CODE)) {
            return vendor.getVendorCode() != null ? vendor.getVendorCode() : "-";
        }
        if (column.equalsIgnoreCase(VendorColumnUtils.VENDOR_CATEGORY)) {
            if (vendor.getVendorCategory() == null) {
                return "-";
            }
            return categoryNamesById.getOrDefault(vendor.getVendorCategory(), "-");
        }
        if (column.equalsIgnoreCase(VendorColumnUtils.CREDIT_LIMIT)) {
            return vendor.getCreditLimit() != null ? String.valueOf(vendor.getCreditLimit()) : "-";
        }
        if (column.equalsIgnoreCase(VendorColumnUtils.CREDIT_PERIOD)) {
            return vendor.getCreditPeriod() != null ? String.valueOf(vendor.getCreditPeriod()) : "-";
        }
        if (column.equalsIgnoreCase(VendorColumnUtils.OUTSTANDING)) {
            return String.valueOf(outstanding(aggregate));
        }
        if (column.equalsIgnoreCase(VendorColumnUtils.LAST_TRANSACTION)) {
            if (aggregate != null && aggregate.lastTransaction() != null) {
                return Utils.dateToString(aggregate.lastTransaction());
            }
            return "NA";
        }
        return "NA";
    }

    private double outstanding(VendorExpenseAggregate aggregate) {
        if (aggregate == null) {
            return 0.0;
        }
        double purchase = aggregate.totalPurchase() != null ? aggregate.totalPurchase() : 0.0;
        double paid = aggregate.totalPaid() != null ? aggregate.totalPaid() : 0.0;
        return purchase - paid;
    }
}
