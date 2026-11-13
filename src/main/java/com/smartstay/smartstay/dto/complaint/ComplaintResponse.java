package com.smartstay.smartstay.dto.complaint;

import com.smartstay.smartstay.filterOptions.complaints.ComplaintFilterOptions;

import java.util.List;

public record ComplaintResponse(String hostelId,
                                String startDate,
                                String endDate,
                                long complaintCount,
                                ComplaintFilterOptions filterOption,
                                List<ComplaintResponseDto> complaintsList) {
}
