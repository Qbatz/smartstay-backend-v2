package com.smartstay.smartstay.payloads.settlement;

import java.util.List;

public record Settlement(Boolean shouldCollectFullRent,
                         Double discountAmount,
                         Double customRent,
                         List<com.smartstay.smartstay.payloads.customer.Settlement> deductions) {
}
