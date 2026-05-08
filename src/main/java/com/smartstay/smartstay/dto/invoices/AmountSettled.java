package com.smartstay.smartstay.dto.invoices;

import java.util.List;

public record AmountSettled(Double totalAmountSettled,
                            int noOfInvoicesApplied,
                            List<AppliedInvoices> redeemdList) {
}
