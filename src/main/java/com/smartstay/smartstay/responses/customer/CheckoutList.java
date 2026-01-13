package com.smartstay.smartstay.responses.customer;

import java.util.List;

public record CheckoutList (String hostelId, int noOfTenants, List<CustomerFilterOption> filterOptions, List<CheckoutCustomers> checkoutCustomers) {
}
