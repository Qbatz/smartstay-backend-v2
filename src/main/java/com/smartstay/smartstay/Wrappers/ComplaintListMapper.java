package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.ComplaintComments;
import com.smartstay.smartstay.dto.complaint.ComplaintResponse;
import com.smartstay.smartstay.dto.complaint.ComplaintResponseDto;
import com.smartstay.smartstay.repositories.ComplaintCommentsRepository;
import com.smartstay.smartstay.repositories.ComplaintRepository;
import com.smartstay.smartstay.responses.complaint.CommentResponse;
import com.smartstay.smartstay.util.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ComplaintListMapper implements Function<Map<String, Object>, ComplaintResponse> {
    private final Map<String, Object> complaintsSummary;
    private final ComplaintCommentsRepository commentsRepository;

    private final String startDate;
    private final String endDate;

    public ComplaintListMapper(String startDate, String endDate, Map<String, Object> complaintsSummary,ComplaintCommentsRepository commentsRepository) {
        this.complaintsSummary = complaintsSummary;
        this.commentsRepository = commentsRepository;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public ComplaintResponse apply(Map<String, Object> raw) {
        ComplaintResponse response = new ComplaintResponse();

        response.setStartDate(startDate);
        response.setEndDate(endDate);
        response.setComplaintCount((Long) complaintsSummary.get("totalComplaints"));

        ComplaintResponseDto dto = new ComplaintResponseDto();
        dto.setComplaintId((Integer) raw.get("complaintId"));
        dto.setCustomerId((String) raw.get("customerId"));
        dto.setCustomerName((String) raw.get("customerName"));
        dto.setCustomerProfile((String) raw.get("customerProfile"));
        dto.setHostelId((String) raw.get("hostelId"));

        dto.setFloorId((Integer) raw.get("floorId"));
        dto.setFloorName((String) raw.get("floorName"));
        dto.setRoomId((Integer) raw.get("roomId"));
        dto.setRoomName((String) raw.get("roomName"));
        dto.setBedId((Integer) raw.get("bedId"));
        dto.setBedName((String) raw.get("bedName"));

        dto.setComplaintDate((Date) raw.get("complaintDate"));
        dto.setAssignedDate((Date) raw.get("assignedDate"));
        dto.setDescription((String) raw.get("description"));
        dto.setAssigneeName((String) raw.get("assigneeName"));
        dto.setAssigneeId((String) raw.get("assigneeId"));

        dto.setComplaintTypeId((Integer) raw.get("complaintTypeId"));
        dto.setComplaintTypeName((String) raw.get("complaintTypeName"));
        dto.setStatus((String) raw.get("status"));


        List<ComplaintComments> complaintComments = commentsRepository.findByComplaint_ComplaintIdAndIsActiveTrue(dto.getComplaintId());
        List<CommentResponse> commentResponses = complaintComments.stream()
                .map(c -> new CommentResponse(
                        c.getCommentId(),
                        c.getComplaint().getComplaintId(),
                        c.getComment(),
                        c.getCreatedBy(),
                        c.getCommentDate()
                ))
                .toList();

        dto.setCommentCount(commentResponses.size());
        dto.setComments(commentResponses);
        response.setComplaintResponseDto(dto);
        return response;
    }
}

