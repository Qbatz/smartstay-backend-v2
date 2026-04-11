package com.smartstay.smartstay.payloads.settlement;

import java.util.List;

public record Settlement(Double discountAmount, List<com.smartstay.smartstay.payloads.customer.Settlement> deductions) {
}
