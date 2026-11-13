package com.smartstay.smartstay.responses.customer;

import java.util.List;

public record CheckoutMobileList(String hostelId,
                                 int noOfTenants,
                                 int currentPage,
                                 int totalPages,
                                 int itemPerPage,
                                 List<CustomerFilterOption> filterOptions,
                                 List<CheckoutCustomers> checkoutCustomers) {
}
