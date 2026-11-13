package com.smartstay.smartstay.responses.electricity;

import java.util.List;

public record ElectricityCustomersMobileList(String hostelId,
                                             int currentPage,
                                             int totalPages,
                                             int itemPerPage,
                                             List<CustomersList> readingList) {
}
