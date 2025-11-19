package com.smartstay.smartstay.dto.customer;

public record CheckoutInfo(String checkoutDate,
                            String requestedDate,
                            String noticeDate,
                            Long noticeDays,
                            String checkoutComments) {
}
