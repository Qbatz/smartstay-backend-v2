package com.smartstay.smartstay.responses.complaint;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    LocalDate getComplaintDate();
    String getDescription();
    String getAssigneeName();

    Integer getComplaintTypeId();
    String getComplaintTypeName();
    String getStatus();

    Integer getCommentCount();
    List<CommentResponse> getComments();
}
