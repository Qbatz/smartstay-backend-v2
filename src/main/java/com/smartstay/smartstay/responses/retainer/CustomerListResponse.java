package com.smartstay.smartstay.responses.retainer;

import com.smartstay.smartstay.responses.invoices.BankInfoRecordPayments;

import java.util.List;

public record CustomerListResponse(String hostelId,
                                   List<CustomersList> customersLists,
                                   List<BankInfoRecordPayments> listBanks) {
}
