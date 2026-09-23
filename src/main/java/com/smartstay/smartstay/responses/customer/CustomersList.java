package com.smartstay.smartstay.responses.customer;

import com.smartstay.smartstay.filterOptions.customers.FilterOptions;
import com.smartstay.smartstay.responses.banking.DebitsBank;

import java.util.List;

public record CustomersList(String hostelId, int noOfTenants, FilterOptions filterOption, List<CustomerData> listCustomers,
                            List<DebitsBank> banks) {
}
