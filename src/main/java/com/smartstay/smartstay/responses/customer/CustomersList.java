package com.smartstay.smartstay.responses.customer;

import com.smartstay.smartstay.filterOptions.customers.FilterOptions;

import java.util.List;

public record CustomersList(String hostelId, int noOfTenants, FilterOptions filterOption, List<CustomerData> listCustomers) {
}
