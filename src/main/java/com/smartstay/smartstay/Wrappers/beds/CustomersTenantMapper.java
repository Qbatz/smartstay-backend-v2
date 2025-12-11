package com.smartstay.smartstay.Wrappers.beds;

import com.smartstay.smartstay.dao.BookingsV1;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.ennum.BookingStatus;
import com.smartstay.smartstay.responses.beds.TenantInfo;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class CustomersTenantMapper implements Function<Customers, TenantInfo> {

    List<BookingsV1> listBookings = null;

    public CustomersTenantMapper(List<BookingsV1> listBookings) {
        this.listBookings = listBookings;
    }

    @Override
    public TenantInfo apply(Customers customers) {

        BookingsV1 bookings = listBookings
                .stream()
                .filter(i -> i.getCustomerId().equalsIgnoreCase(customers.getCustomerId()))
                .findFirst()
                .orElse(null);

        String tenantId = null;
        String firstName = null;
        String lastName = null;
        String profilePic = null;
        StringBuilder fullName = new StringBuilder();
        StringBuilder initials = new StringBuilder();
        String joiningDate = null;
        String bookingDate = null;
        String mobile = null;
        double advance = 0.0;
        double bookingAmount = 0.0;
        Double rentAmount = null;
        Double lastInvoiceAmount = null;
        String lastInvoiceNumber = null;
        int totalInvoice = 0;
        String leavingDate = null;
        String currentStatus = BookingStatus.BOOKED.name();

        if (bookings != null) {
            joiningDate = Utils.dateToString(bookings.getExpectedJoiningDate());
            bookingDate = Utils.dateToString(bookings.getBookingDate());
            rentAmount = bookings.getRentAmount();
            bookingAmount = bookings.getBookingAmount();
        }



        if (customers != null) {
            tenantId = customers.getCustomerId();
            firstName = customers.getFirstName();
            lastName = customers.getLastName();
            profilePic = customers.getProfilePic();
            mobile = customers.getMobile();

            if (customers.getFirstName() != null) {
                fullName.append(customers.getFirstName());
                initials.append(customers.getFirstName().toUpperCase().charAt(0));
            }
            if (customers.getLastName() != null && !customers.getLastName().trim().equalsIgnoreCase("")) {
                fullName.append(" ");
                fullName.append(customers.getLastName());
                initials.append(customers.getLastName().toUpperCase().charAt(0));
            }
            else if (customers.getFirstName() != null) {
                if (customers.getFirstName().length() > 1) {
                    initials.append(customers.getFirstName().toUpperCase().charAt(1));
                }
            }

        }

        return new TenantInfo(tenantId,
                firstName,
                lastName,
                fullName.toString(),
                profilePic,
                initials.toString(),
                joiningDate,
                bookingDate,
                bookingAmount,
                mobile,
                advance,
                rentAmount,
                null,
                null,
                totalInvoice,
                null,
                currentStatus,
                Utils.COUNTRY_CODE);

    }
}
