package com.smartstay.smartstay.responses.settlement;

import com.smartstay.smartstay.dto.customer.Deductions;

import java.util.List;

public record DeductionsInfo(Double totalDeductionsAmount,
                             Double paidAmount,
                             Double pendingAmount,
                             List<DeductionsItem> listDeductions) {
}
