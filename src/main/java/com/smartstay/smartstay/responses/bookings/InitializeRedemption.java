package com.smartstay.smartstay.responses.bookings;

import java.util.List;

public record InitializeRedemption(AdvanceInfo advanceInfo,
                                   CustomerInfo customerInfo,
                                   List<InitializeInvoiceItems> listInvoices) {
}
