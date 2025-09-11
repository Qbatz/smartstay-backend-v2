package com.smartstay.smartstay.responses.complaint;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;

import static com.smartstay.smartstay.util.Utils.OUTPUT_DATE_FORMAT;

public interface ComplaintResponse {
    Integer getComplaintId();
    String getCustomerId();
    String getCustomerName();
    String getCustomerProfile();
    String getHostelId();

    Integer getFloorId();
    String getFloorName();

    Integer getRoomId();
    String getRoomName();

    Integer getBedId();
    String getBedName();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = OUTPUT_DATE_FORMAT, timezone = "Asia/Kolkata")
    Date getComplaintDate();
    String getDescription();
    String getAssigneeName();

    Integer getComplaintTypeId();
    String getComplaintTypeName();
    String getStatus();

    Integer getCommentCount();
    List<CommentResponse> getComments();
}
