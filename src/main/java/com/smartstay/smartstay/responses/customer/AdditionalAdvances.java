package com.smartstay.smartstay.responses.customer;

import java.util.List;

public record AdditionalAdvances(Double totalAmount,
                                 Double paidAmount,
                                 Double advanceBalances,
                                 int totalAdvances,
                                 List<AdditionalAdvanceItems> listInvoices) {
}
