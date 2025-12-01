package com.smartstay.smartstay.dto.complaint;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public record ComplaintResponse(String startDate, String endDate, long complaintCount, List<ComplaintResponseDto> complaintsList) {
}
