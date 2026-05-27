package com.smartstay.smartstay.responses.settlement;

public record DeductionsItem(String item,
                             Double paidAmount,
                             Double amount,
                             Double pendingAmount) {
}
