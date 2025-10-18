package com.smartstay.smartstay.responses.customer;

import java.util.List;

public record CustomersList(List<CustomerData> listCustomers, List<CustomerFilterOption> filterOption) {
}
