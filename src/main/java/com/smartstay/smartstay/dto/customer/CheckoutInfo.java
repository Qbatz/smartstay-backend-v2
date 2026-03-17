package com.smartstay.smartstay.dto.customer;

public record CheckoutInfo(String checkoutDate,
                            String requestedLeavingDate,
                            String noticeDate,
                            Long noticeDays,
                            String checkoutComments) {
}
