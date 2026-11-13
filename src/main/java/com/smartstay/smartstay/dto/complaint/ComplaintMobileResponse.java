package com.smartstay.smartstay.dto.complaint;

import com.smartstay.smartstay.filterOptions.complaints.ComplaintFilterOptions;

import java.util.List;

public record ComplaintMobileResponse(String hostelId,
                                      String startDate,
                                      String endDate,
                                      long complaintCount,
                                      int currentPage,
                                      int totalPages,
                                      int itemPerPage,
                                      ComplaintFilterOptions filterOption,
                                      List<ComplaintResponseDto> complaintsList) {
}
