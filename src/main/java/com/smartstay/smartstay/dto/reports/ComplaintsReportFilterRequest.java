package com.smartstay.smartstay.dto.reports;

import com.smartstay.smartstay.ennum.ComplaintStatus;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ComplaintsReportFilterRequest {
    private String startDate;
    private String endDate;
    private List<ComplaintStatus> status;
    private String period;
    private List<String> raisedBy;
    private List<Integer> complaintTypeIds;
    private int page;
    private int size;
}
