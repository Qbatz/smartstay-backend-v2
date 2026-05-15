package com.smartstay.smartstay.Wrappers.booking;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.booking.BookingTableHeader;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;
import com.smartstay.smartstay.util.columnOptions.BookingColumnUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BookingsTableMapper implements Function<InvoicesV1, List<Object>> {

    private List<String> tableColumns = null;
    private List<Customers> listCustomers = null;
    private List<Rooms> listRooms = null;
    private List<Floors> listFloors = null;
    private List<BookingsV1> listBookings = null;
    private List<BedDetails> listBedDetails = null;

    public BookingsTableMapper(List<String> tableColumns, List<Customers> listCustomers, List<Rooms> listRooms, List<Floors> listFloors, List<BookingsV1> listBookings, List<BedDetails> listBedDetails) {
        this.tableColumns = tableColumns;
        this.listCustomers = listCustomers;
        this.listRooms = listRooms;
        this.listFloors = listFloors;
        this.listBookings = listBookings;
        this.listBedDetails = listBedDetails;
    }

    @Override
    public List<Object> apply(InvoicesV1 invoicesV1) {
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
        BookingTableHeader bookingTableHeader = new BookingTableHeader(invoicesV1.getInvoiceId(),
                canApplyToOtherInvoices,
                invoicesV1.getBalanceAmount());

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
                return customers.getCustomerId();
            }
            return NameUtils.getInitials(customers.getFirstName(), customers.getLastName());
        }
        else if (columnName.equalsIgnoreCase(BookingColumnUtils.MOBILE_NO)) {
            return "+91 " + customers.getMobile();
        }
        else if (columnName.equalsIgnoreCase(BookingColumnUtils.AMOUNT)) {
            return String.valueOf(invoicesV1.getTotalAmount());
        }
        else if (columnName.equalsIgnoreCase(BookingColumnUtils.JOINING_DATE)) {
            return Utils.dateToString(bookingsV1.getExpectedJoiningDate());
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
        return "NA";
    }
}
