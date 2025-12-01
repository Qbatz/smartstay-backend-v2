package com.smartstay.smartstay.dto.complaint;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.smartstay.smartstay.responses.complaint.CommentResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.smartstay.smartstay.util.Utils.OUTPUT_DATE_FORMAT;

@Setter
@Getter
public class ComplaintResponseDto {
    private Integer complaintId;
    private String customerId;
    private String customerName;
    private String customerProfile;
    private String hostelId;
    private String initials;

    private Integer floorId;
    private String floorName;

    private Integer roomId;
    private String roomName;

    private Integer bedId;
    private String bedName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = OUTPUT_DATE_FORMAT, timezone = "Asia/Kolkata")
    private java.util.Date complaintDate;
    private String description;
    private String assigneeId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = OUTPUT_DATE_FORMAT, timezone = "Asia/Kolkata")
    private java.util.Date assignedDate;
    private String assigneeName;

    private Integer complaintTypeId;
    private String complaintTypeName;
    private String status;


}
