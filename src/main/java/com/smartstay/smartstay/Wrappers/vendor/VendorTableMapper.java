package com.smartstay.smartstay.Wrappers.vendor;

import com.smartstay.smartstay.dao.VendorV1;
import com.smartstay.smartstay.ennum.VendorPaymentStatus;
import com.smartstay.smartstay.responses.vendor.VendorHeaderAdditionalFields;
import com.smartstay.smartstay.util.AddressUtils;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;
import com.smartstay.smartstay.util.columnOptions.VendorColumnUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Maps a {@link VendorV1} into an ordered list of cell values, one per visible/configured column,
 * followed by a trailing {@link VendorHeaderAdditionalFields} object carrying the vendor id.
 *
 * <p>Financial cells read the denormalized fields on the vendor row directly (no runtime
 * aggregation). The "Last Transaction" date is supplied as a pre-built map (latest payment per
 * vendor) so the listing resolves it with a single bulk query rather than per-row lookups.
 */
public class VendorTableMapper implements Function<VendorV1, List<Object>> {

    private final List<String> columns;
    private final Map<Integer, String> categoryNamesById;
    private final Map<String, Date> lastPaymentByVendorId;

    public VendorTableMapper(List<String> columns,
                             Map<Integer, String> categoryNamesById,
                             Map<String, Date> lastPaymentByVendorId) {
        this.columns = columns != null ? columns : List.of();
        this.categoryNamesById = categoryNamesById != null ? categoryNamesById : Collections.emptyMap();
        this.lastPaymentByVendorId = lastPaymentByVendorId != null ? lastPaymentByVendorId : Collections.emptyMap();
    }

    @Override
    public List<Object> apply(VendorV1 vendor) {
        List<Object> columnItems = new ArrayList<>();
        columns.forEach(column -> columnItems.add(getColumnItem(vendor, column)));
        VendorPaymentStatus status = vendor.getPaymentStatus() != null
                ? vendor.getPaymentStatus() : VendorPaymentStatus.NO_TRANSACTION;
        columnItems.add(new VendorHeaderAdditionalFields(String.valueOf(vendor.getVendorId()), status.name()));
        return columnItems;
    }

    private String getColumnItem(VendorV1 vendor, String column) {
        if (column.equalsIgnoreCase(VendorColumnUtils.PROFILE_PIC)) {
            if (vendor.getProfilePic() != null && !vendor.getProfilePic().trim().isEmpty()) {
                return vendor.getProfilePic();
            }
            return NameUtils.getInitials(vendor.getFirstName(), vendor.getLastName());
        }
        if (column.equalsIgnoreCase(VendorColumnUtils.FULL_NAME)) {
            return NameUtils.getFullName(vendor.getFirstName(), vendor.getLastName());
        }
        if (column.equalsIgnoreCase(VendorColumnUtils.PAYMENT_STATUS)) {
            VendorPaymentStatus status = vendor.getPaymentStatus() != null
                    ? vendor.getPaymentStatus() : VendorPaymentStatus.NO_TRANSACTION;
            return status.getDisplayName();
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
            // Pre-computed and stored on the vendor row; no runtime aggregation.
            return String.valueOf(vendor.getBalance() != null ? vendor.getBalance() : 0.0);
        }
        if (column.equalsIgnoreCase(VendorColumnUtils.LAST_TRANSACTION)) {
            Date lastPayment = lastPaymentByVendorId.get(String.valueOf(vendor.getVendorId()));
            return lastPayment != null ? Utils.dateToString(lastPayment) : "NA";
        }
        if (column.equalsIgnoreCase(VendorColumnUtils.BUSINESS_NAME)) {
            return vendor.getBusinessName() != null && !vendor.getBusinessName().trim().isEmpty()
                    ? vendor.getBusinessName() : "-";
        }
        if (column.equalsIgnoreCase(VendorColumnUtils.VENDOR_ADDRESS)) {
            String address = AddressUtils.formatAddress(vendor.getHouseNo(), vendor.getArea(), vendor.getLandMark(),
                    vendor.getCity(), vendor.getPinCode(), vendor.getState());
            return address.isEmpty() ? "-" : address;
        }
        return "NA";
    }
}
