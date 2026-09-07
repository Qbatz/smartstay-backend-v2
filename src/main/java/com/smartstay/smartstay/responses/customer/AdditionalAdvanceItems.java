package com.smartstay.smartstay.responses.customer;

public record AdditionalAdvanceItems(String invoiceNumber,
                                     String invoiceId,
                                     Double invoiceAmount,
                                     Double paidAmount,
                                     Double invoiceBalance) {
}
