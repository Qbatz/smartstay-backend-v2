package com.smartstay.smartstay.dto.complaint;

import java.util.List;

public record ComplaintMobileResponse(String hostelId,
                                      String startDate,
                                      String endDate,
                                      long complaintCount,
                                      int currentPage,
                                      int totalPages,
                                      int itemPerPage,
                                      List<ComplaintResponseDto> complaintsList) {
}
