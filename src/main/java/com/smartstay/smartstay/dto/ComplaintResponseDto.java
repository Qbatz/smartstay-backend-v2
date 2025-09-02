package com.smartstay.smartstay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.smartstay.smartstay.responses.complaint.CommentResponse;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ComplaintResponseDto {
    private Integer complaintId;
    private String customerId;
    private String customerName;
    private String customerProfile;
    private String hostelId;

    private Integer floorId;
    private String floorName;

    private Integer roomId;
    private String roomName;

    private Integer bedId;
    private String bedName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private java.util.Date complaintDate;
    private String description;
    private String assigneeName;

    private Integer complaintTypeId;
    private String complaintTypeName;
    private String status;

    private Integer commentCount;
    private List<CommentResponse> comments = new ArrayList<>();


}
