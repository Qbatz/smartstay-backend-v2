package com.smartstay.smartstay.responses.customer;

import java.util.List;

public record CustomersList(String hostelId, int noOfTenants, List<CustomerFilterOption> filterOption, List<CustomerData> listCustomers) {
}
