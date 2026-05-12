package com.smartstay.smartstay.Wrappers.booking;

import com.smartstay.smartstay.dao.BookingsV1;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.ennum.BookingStatus;
import com.smartstay.smartstay.responses.bookings.AdvanceListItems;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class AdvanceInvoicesMapper implements Function<InvoicesV1, AdvanceListItems> {
    private List<BookingsV1> listBookings = null;
    private List<BedDetails> listBedDetails = null;
    private List<Customers> listCustomers = null;

    public AdvanceInvoicesMapper(List<BookingsV1> listBookings, List<BedDetails> listBedDetails, List<Customers> listCustomers) {
        this.listBookings = listBookings;
        this.listBedDetails = listBedDetails;
        this.listCustomers = listCustomers;
    }

    @Override
    public AdvanceListItems apply(InvoicesV1 invoicesV1) {
        String firstName = null;
        String lastName = null;
        String fullName = null;
        String initials = null;
        String profilePic = null;
        Double paidAmount = 0.0;
        Double pendingAmount = 0.0;
        String bookingDate = null;
        String customerMobile = null;
        String floorName = null;
        String bedName = null;
        String roomName = null;
        boolean canRedeem = true;

        if (listCustomers != null) {
            Customers customers = listCustomers
                    .stream()
                    .filter(i -> i.getCustomerId().equalsIgnoreCase(invoicesV1.getCustomerId()))
                    .findFirst()
                    .orElse(null);
            if (customers != null) {
                firstName = customers.getFirstName();
                lastName = customers.getLastName();
                fullName = NameUtils.getFullName(customers.getFirstName(), customers.getLastName());
                initials = NameUtils.getInitials(customers.getFirstName(), customers.getLastName());
                profilePic = customers.getProfilePic();
                customerMobile = customers.getMobile();

            }
        }

        if (listBookings != null) {
            BookingsV1 bookingsV1 = listBookings
                    .stream()
                    .filter(i -> i.getCustomerId().equalsIgnoreCase(invoicesV1.getCustomerId()))
                    .findFirst()
                    .orElse(null);
            if (bookingsV1 != null) {
                if (bookingsV1.getIsBooked() != null && bookingsV1.getIsBooked()) {
                    bookingDate = Utils.dateToString(bookingsV1.getBookingDate());
                }

                BedDetails bedDetails = listBedDetails.stream()
                        .filter(i -> i.getBedId().equals(bookingsV1.getBedId()))
                        .findFirst()
                        .orElse(null);
                if (bedDetails != null) {
                    if (bedDetails.getBedName() == null ||
                            bedDetails.getBedName().trim().equalsIgnoreCase("")
                    || bedDetails.getBedName().equalsIgnoreCase("null")) {
                        bedName = "NA";
                        floorName = "NA";
                        roomName = "NA";
                    }
                    else {
                        bedName = bedDetails.getBedName();
                        floorName = bedDetails.getFloorName();
                        roomName = bedDetails.getRoomName();
                    }
                }
                else {
                    bedName = "NA";
                    floorName = "NA";
                    roomName = "NA";
                }

                if (bookingsV1.getCurrentStatus().equalsIgnoreCase(BookingStatus.BOOKED.name())) {
                    canRedeem = false;
                }
            }
        }

        if (canRedeem) {
            if (invoicesV1.getBalanceAmount() == null || invoicesV1.getBalanceAmount() <= 0) {
                canRedeem = false;
            }
        }


        return new AdvanceListItems(invoicesV1.getInvoiceId(),
                invoicesV1.getInvoiceNumber(),
                invoicesV1.getInvoiceType(),
                invoicesV1.getTotalAmount(),
                invoicesV1.getBalanceAmount(),
                Utils.dateToString(invoicesV1.getInvoiceStartDate()),
                bookingDate,
                firstName,
                lastName,
                fullName,
                initials,
                profilePic,
                "+91 " + customerMobile,
                floorName,
                bedName,
                roomName,
                canRedeem);
    }
}
