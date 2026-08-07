package com.smartstay.smartstay.dto.retainer;

public record RetainerItems(String invoiceId,
                            String invoiceNumber,
                            String invoiceDate,
                            String paidDate,
                            Double invoiceAmount,
                            Double redeemedAmount,
                            Double availableAmount) {
}
