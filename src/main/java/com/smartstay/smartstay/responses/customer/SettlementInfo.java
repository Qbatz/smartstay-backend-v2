package com.smartstay.smartstay.responses.customer;

public record SettlementInfo(Double amountTobePaid,
                             Double totalDeductions,
                             Double payableRent,
                             Double refundableRent,
                             Double refundableAvance,
                             boolean isRefundable) {
}
