package com.smartstay.smartstay.responses.customer;

public record SettlementInfo(Double amountTobePaid,
                             Double totalDeductions,
                             Double payableRent,
                             boolean isRefundable) {
}
