package com.smartstay.smartstay.responses.customer;

public record SettlementInfo(Double amountTobePaid,
                             Double totalDeductions,
                             Double payableRent,
                             Double refundableRent,
                             Double refundableAdvance,
                             Double electricityAmount,
                             Double unpaidInvoiceAmount,
                             boolean isRefundable,
                             String label,
                             double payableAmount) {
}
