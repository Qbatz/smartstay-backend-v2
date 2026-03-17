package com.smartstay.smartstay.responses.recurring;

import java.util.List;

public record RecurringList(String hostelId, List<CustomersList> customers) {
}
