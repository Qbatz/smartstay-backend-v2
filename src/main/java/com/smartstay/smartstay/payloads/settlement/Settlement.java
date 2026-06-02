package com.smartstay.smartstay.payloads.settlement;

import java.util.List;

public record Settlement(Boolean shouldCollectFullRent, Double discountAmount, List<com.smartstay.smartstay.payloads.customer.Settlement> deductions) {
}
