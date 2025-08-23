package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.ComplaintsV1;
import com.smartstay.smartstay.responses.complaint.ComplaintResponse;

import java.util.function.Function;

public class ComplaintMapper implements Function<ComplaintsV1, ComplaintResponse> {
    @Override
    public ComplaintResponse apply(ComplaintsV1 complaints) {
        return new ComplaintResponse(
                complaints.getComplaintId(),
                complaints.getComplaintDate(),
                complaints.getStatus(),
                complaints.getComplaintType()
        );
    }


}
