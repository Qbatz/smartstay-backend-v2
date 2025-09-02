package com.smartstay.smartstay.dto.complaint;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ComplaintResponse {

    private String startDate;
    private String endDate;
    private Integer complaintCount;

    private ComplaintResponseDto complaintResponseDto;
}
