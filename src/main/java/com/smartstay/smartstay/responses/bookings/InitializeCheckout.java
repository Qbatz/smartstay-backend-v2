package com.smartstay.smartstay.responses.bookings;

public record InitializeCheckout(boolean canCheckout, String checkoutDate, String joiningDate) {
}
