package com.smartstay.smartstay.payloads.invoice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record InvoiceRedemption(String reason,
                                String date,
                                List<RedemptionItems> listItems) {

}
