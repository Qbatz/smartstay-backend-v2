package com.smartstay.smartstay.responses.bookings;

import com.smartstay.smartstay.dto.retainer.RetainerSummary;

import java.util.List;

public record InitializeRedemption(AdvanceInfo advanceInfo,
                                   RetainerSummary retainerSummary,
                                   CustomerInfo customerInfo,
                                   List<InitializeInvoiceItems> listInvoices) {
}
