package com.smartstay.smartstay.Wrappers.booking;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.booking.BookingTableHeader;
import com.smartstay.smartstay.ennum.CustomerStatus;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;
import com.smartstay.smartstay.util.columnOptions.BookingColumnUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BookingsTableMapper implements Function<InvoicesV1, List<Object>> {

    private List<String> tableColumns = null;
    private List<Customers> listCustomers = null;
    private List<BookingsV1> listBookings = null;
    private List<BedDetails> listBedDetails = null;

    public BookingsTableMapper(List<String> tableColumns, List<Customers> listCustomers,  List<BookingsV1> listBookings, List<BedDetails> listBedDetails) {
        this.tableColumns = tableColumns;
        this.listCustomers = listCustomers;
        this.listBookings = listBookings;
        this.listBedDetails = listBedDetails;
    }

    @Override
    public List<Object> apply(InvoicesV1 invoicesV1) {
        String status = getStatus(invoicesV1);
        List<Object> columnItems = new ArrayList<>();
        BookingsV1 bookingsV1 = listBookings
                .stream()
                .filter(i -> i.getCustomerId().equalsIgnoreCase(invoicesV1.getCustomerId()))
                .findFirst()
                .orElse(null);
        Customers customers = listCustomers
                .stream()
                .filter(i -> i.getCustomerId().equalsIgnoreCase(invoicesV1.getCustomerId()))
                .findFirst()
                .orElse(null);
        BedDetails bedDetails;
        if (bookingsV1 != null) {
            bedDetails = listBedDetails
                    .stream()
                    .filter(i -> i.getBedId().equals(bookingsV1.getBedId()))
                    .findFirst()
                    .orElse(null);
        } else {
            bedDetails = null;
        }

        if (tableColumns != null) {
            tableColumns.forEach(i -> columnItems.add(getColumnFields(i, bookingsV1, customers, invoicesV1, bedDetails)));
        }

        boolean canApplyToOtherInvoices = false;
        if (!invoicesV1.isCancelled()) {
            if (invoicesV1.getBalanceAmount() > 0) {
                canApplyToOtherInvoices = true;
            }
        }
        if (canApplyToOtherInvoices) {
            if (customers != null) {
                if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.VACATED.name())) {
                    canApplyToOtherInvoices = false;
                }
                if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
                    canApplyToOtherInvoices = false;
                }
                if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CANCELLED_BOOKING.name())) {
                    canApplyToOtherInvoices = false;
                }
            }
        }
        BookingTableHeader bookingTableHeader = new BookingTableHeader(invoicesV1.getInvoiceId(),
                canApplyToOtherInvoices,
                invoicesV1.getBalanceAmount(),
                invoicesV1.getCustomerId(),
                status);

        columnItems.add(bookingTableHeader);
        return columnItems;
    }

    public String getColumnFields(String columnName, BookingsV1 bookingsV1, Customers customers, InvoicesV1 invoicesV1, BedDetails bedDetails) {
        if (columnName.equalsIgnoreCase(BookingColumnUtils.INVOICE_NUMBER)) {
            return invoicesV1.getInvoiceNumber();
        }
        else if (columnName.equalsIgnoreCase(BookingColumnUtils.BOOKING_DATE)) {
            return Utils.dateToString(bookingsV1.getBookingDate());
        }
        else if (columnName.equalsIgnoreCase(BookingColumnUtils.TENANT_NAME)) {
            return NameUtils.getFullName(customers.getFirstName(), customers.getLastName());
        }
        else if (columnName.equalsIgnoreCase(BookingColumnUtils.PROFILE_PIC)) {
            if (customers.getProfilePic() != null) {
                return customers.getProfilePic();
            }
            return NameUtils.getInitials(customers.getFirstName(), customers.getLastName());
        }
        else if (columnName.equalsIgnoreCase(BookingColumnUtils.MOBILE_NO)) {
            return "+91 " + customers.getMobile();
        }
        else if (columnName.equalsIgnoreCase(BookingColumnUtils.AMOUNT)) {
            return "₹" + invoicesV1.getTotalAmount();
        }
        else if (columnName.equalsIgnoreCase(BookingColumnUtils.JOINING_DATE)) {
            return Utils.dateToString(bookingsV1.getJoiningDate());
        }
        else if (columnName.equalsIgnoreCase(BookingColumnUtils.FLOOR)) {
            return bedDetails.getFloorName();
        }
        else if (columnName.equalsIgnoreCase(BookingColumnUtils.ROOM)) {
            return bedDetails.getRoomName();
        }
        else if (columnName.equalsIgnoreCase(BookingColumnUtils.BED)) {
            return bedDetails.getBedName();
        }
        else if (columnName.equalsIgnoreCase(BookingColumnUtils.BALANCE_AMOUNT)) {
            return "₹" + invoicesV1.getBalanceAmount();
        }
        else if (columnName.equalsIgnoreCase(BookingColumnUtils.STATUS)) {
            if (invoicesV1.getBalanceAmount() != null) {
                if (invoicesV1.getBalanceAmount().equals(invoicesV1.getTotalAmount())) {
                    return "Available";
                }
                else if (invoicesV1.getBalanceAmount() < invoicesV1.getTotalAmount()) {
                    if (invoicesV1.getBalanceAmount() == 0) {
                        return "Redeemed";
                    }
                    return "Partially Redeemed";
                }
                else {
                    return "Redeemed";
                }
            }
            return "Redeemed";
        }
        return "NA";
    }

    public String getStatus(InvoicesV1 invoicesV1) {
        if (invoicesV1.getBalanceAmount() != null) {
            if (invoicesV1.getBalanceAmount() == invoicesV1.getTotalAmount()) {
                return "Available";
            }
            else if (invoicesV1.getBalanceAmount() < invoicesV1.getTotalAmount()) {
                return "Partially Redeemed";
            }
            else {
                return "Redeemed";
            }
        }
        return "Not Available";
    }
}
