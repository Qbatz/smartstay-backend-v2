package com.smartstay.smartstay.Wrappers.customers;

import com.smartstay.smartstay.dao.BookingsV1;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.Draft;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.ennum.BookingStatus;
import com.smartstay.smartstay.ennum.CustomerStatus;
import com.smartstay.smartstay.responses.customer.HeaderAdditionalFields;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.columnOptions.TenantColumnUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TenantTableMapper implements Function<Customers, List<Object>> {
    private final List<BedDetails> listBedDetails;
    private final List<BookingsV1> listBookings;
    private final List<String> columns;
    private final Map<String, Draft> draftsByCustomerId;

    public TenantTableMapper(List<BedDetails> listBedDetails, List<BookingsV1> listBookings, List<String> columns) {
        this(listBedDetails, listBookings, columns, Collections.emptyMap());
    }

    public TenantTableMapper(List<BedDetails> listBedDetails, List<BookingsV1> listBookings, List<String> columns,
                             Map<String, Draft> draftsByCustomerId) {
        this.listBedDetails = listBedDetails != null ? listBedDetails : List.of();
        this.listBookings = listBookings != null ? listBookings : List.of();
        this.columns = columns;
        this.draftsByCustomerId = draftsByCustomerId != null ? draftsByCustomerId : Collections.emptyMap();
    }

    @Override
    public List<Object> apply(Customers customers) {
        List<Object> columnItems = new ArrayList<>();
        BookingsV1 bookingsV1 = listBookings.stream()
                .filter(i -> i.getCustomerId().equalsIgnoreCase(customers.getCustomerId()))
                .findFirst()
                .orElse(null);

        final Draft draft = draftsByCustomerId.get(customers.getCustomerId());

        final BedDetails bedDetail;
        if (bookingsV1 != null) {
            bedDetail = listBedDetails.stream()
                    .filter(i -> i.getBedId().equals(bookingsV1.getBedId()))
                    .findFirst()
                    .orElse(null);
        } else if (draft != null && draft.getBedId() != null) {
            bedDetail = listBedDetails.stream()
                    .filter(i -> i.getBedId().equals(draft.getBedId()))
                    .findFirst()
                    .orElse(null);
        } else {
            bedDetail = null;
        }

        if (columns != null) {
            columns.forEach(i -> columnItems.add(getColumnItem(customers, bedDetail, bookingsV1, draft, i)));
        }

        HeaderAdditionalFields additionalFields = new HeaderAdditionalFields(customers.getCustomerId(),
                getColumnItem(customers, null, bookingsV1, draft, TenantColumnUtils.STATUS));
        columnItems.add(additionalFields);

        return columnItems;
    }

    private String getColumnItem(Customers customers, BedDetails bedDetail, BookingsV1 bookingsV1, Draft draft, String columnItem) {
        if (columnItem.equalsIgnoreCase(TenantColumnUtils.PROFILE_PIC)) {
            if (customers.getProfilePic() != null && !customers.getProfilePic().trim().equalsIgnoreCase("")) {
                return customers.getProfilePic();
            }
            return NameUtils.getInitials(customers.getFirstName(), customers.getLastName());
        }
        if (columnItem.equalsIgnoreCase(TenantColumnUtils.FULL_NAME)) {
            return NameUtils.getFullName(customers.getFirstName(), customers.getLastName());
        }
        if (columnItem.equalsIgnoreCase(TenantColumnUtils.STATUS)) {
            if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.BOOKED.name())) {
                return "Booked";
            } else if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.VACATED.name())) {
                return "Vacated";
            } else if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.NOTICE.name())) {
                return "Notice Period";
            } else if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CHECK_IN.name())) {
                return "Checked In";
            } else if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.INACTIVE.name())) {
                return "Inactive";
            } else if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.ACTIVE.name())) {
                return "Active";
            } else if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CANCELLED_BOOKING.name())) {
                return "Cancelled";
            } else if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
                return "Settlement Generated";
            } else if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.DRAFT.name())) {
                return "Draft";
            }
            return "Walk In";
        }
        if (columnItem.equalsIgnoreCase(TenantColumnUtils.JOINING_DATE)) {
            if (bookingsV1 != null) {

                if (bookingsV1.getCurrentStatus() != null && bookingsV1.getCurrentStatus().equalsIgnoreCase(BookingStatus.CHECKIN.name())) {
                    if (bookingsV1.getJoiningDate() != null) {
                        return Utils.dateToString(bookingsV1.getJoiningDate());
                    }
                }
                if (bookingsV1.getCurrentStatus() != null && bookingsV1.getCurrentStatus().equalsIgnoreCase(BookingStatus.BOOKED.name())) {
                    if (bookingsV1.getExpectedJoiningDate() != null) {
                        return Utils.dateToString(bookingsV1.getExpectedJoiningDate());
                    }
                }
                if (bookingsV1.getCurrentStatus() != null && bookingsV1.getCurrentStatus().equalsIgnoreCase(BookingStatus.NOTICE.name())) {
                    if (bookingsV1.getJoiningDate() != null) {
                        return Utils.dateToString(bookingsV1.getJoiningDate());
                    }
                }
                if (bookingsV1.getCurrentStatus() != null && bookingsV1.getCurrentStatus().equalsIgnoreCase(BookingStatus.VACATED.name())) {
                    if (bookingsV1.getJoiningDate() != null) {
                        return Utils.dateToString(bookingsV1.getJoiningDate());
                    }
                }
            }
            if (draft != null && draft.getJoiningDate() != null) {
                return Utils.dateToString(draft.getJoiningDate());
            }
            if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.DRAFT.name()) && customers.getExpJoiningDate() != null) {
                return Utils.dateToString(customers.getExpJoiningDate());
            }
            return "NA";
        }
        if (columnItem.equalsIgnoreCase(TenantColumnUtils.MOBILE_NUMBER)) {
            return "+91 " + customers.getMobile();
        }
        if (columnItem.equalsIgnoreCase(TenantColumnUtils.FLOOR)) {
            if (bedDetail != null) {
                if (bedDetail.getFloorName() == null) {
                    return "-";
                }
                return bedDetail.getFloorName();
            }
            return "NA";
        }
        if (columnItem.equalsIgnoreCase(TenantColumnUtils.ROOM)) {
            if (bedDetail != null) {
                if (bedDetail.getRoomName() == null) {
                    return "-";
                }
                return bedDetail.getRoomName();
            }
            return "NA";
        }
        if (columnItem.equalsIgnoreCase(TenantColumnUtils.BED)) {
            if (bedDetail != null) {
                if (bedDetail.getBedName() == null) {
                    return "-";
                }
                return bedDetail.getBedName();
            }
            return "NA";
        }
        if (columnItem.equalsIgnoreCase(TenantColumnUtils.EMAIL_ID)) {
            if (customers.getEmailId() == null) {
                return "-";
            }
            return customers.getEmailId();
        }
        if (columnItem.equalsIgnoreCase(TenantColumnUtils.MONTHLY_RENT)) {
            if (bookingsV1 != null && bookingsV1.getRentAmount() != null) {
                return String.valueOf(bookingsV1.getRentAmount());
            }
            if (draft != null && draft.getRentalAmount() != null) {
                return String.valueOf(draft.getRentalAmount());
            }
            return "-";
        }
        if (columnItem.equalsIgnoreCase(TenantColumnUtils.ADVANCE)) {
            if (bookingsV1 != null && bookingsV1.getAdvanceAmount() != null) {
                return String.valueOf(bookingsV1.getAdvanceAmount());
            }
            if (draft != null && draft.getAdvanceAmount() != null) {
                return String.valueOf(draft.getAdvanceAmount());
            }
            return "-";
        }
        if (columnItem.equalsIgnoreCase(TenantColumnUtils.BOOKING_AMOUNT)) {
            if (bookingsV1 != null && bookingsV1.getBookingAmount() != null) {
                return String.valueOf(bookingsV1.getBookingAmount());
            }
            if (draft != null && draft.getBookingAmount() != null) {
                return String.valueOf(draft.getBookingAmount());
            }
            return "-";
        }

        return "NA";
    }
}
