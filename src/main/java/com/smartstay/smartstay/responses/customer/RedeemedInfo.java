package com.smartstay.smartstay.responses.customer;

public record RedeemedInfo(String invoiceId,
                           String invoiceNumber,
                           String date,
                           String invoiceType,
                           Double redeemedAmount,
                           Double invoiceAmount) {
}
