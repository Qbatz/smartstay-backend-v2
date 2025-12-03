package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.ComplaintComments;
import com.smartstay.smartstay.dao.ComplaintTypeV1;
import com.smartstay.smartstay.dao.ComplaintsV1;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dto.complaint.ComplaintResponse;
import com.smartstay.smartstay.dto.complaint.ComplaintResponseDto;
import com.smartstay.smartstay.dto.room.RoomInfo;
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

public class ComplaintListMapper implements Function<ComplaintsV1, ComplaintResponseDto> {

    List<Customers> customersList = null;
    List<RoomInfo> roomInfos = null;
    List<ComplaintTypeV1> complaintTypes = null;

    public ComplaintListMapper(List<Customers> customersList, List<RoomInfo> roomInfo, List<ComplaintTypeV1> complaintTypes) {
        this.customersList = customersList;
        this.roomInfos = roomInfo;
        this.complaintTypes = complaintTypes;
    }
    @Override
    public ComplaintResponseDto apply(ComplaintsV1 complaintsV1) {

        String customerId = null;
        StringBuilder fullName = new StringBuilder();
        StringBuilder initials = new StringBuilder();
        String profilePic = null;
        String roomName = null;
        String floorName = null;
        String bedName = null;
        String complaintTypeName = null;

        if (roomInfos != null && complaintsV1.getRoomId() != null) {
            RoomInfo roomInfo = roomInfos
                    .stream()
                    .filter(i -> i.getRoomId().equals(complaintsV1.getRoomId()))
                    .findFirst()
                    .orElse(null);

            if (roomInfo != null) {
                roomName = roomInfo.getRoomName();
                floorName = roomInfo.getFloorName();
            }
        }

        Customers customers = customersList
                .stream()
                .filter(i -> i.getCustomerId().equalsIgnoreCase(complaintsV1.getCustomerId()))
                .limit(1)
                .findFirst()
                .orElse(null);

        if (customers != null) {
            customerId = customers.getCustomerId();
            profilePic = customers.getProfilePic();

            if (customers.getFirstName() != null) {
                initials.append(customers.getFirstName().toUpperCase().charAt(0));
                fullName.append(customers.getFirstName());
            }
            if (customers.getLastName() != null && !customers.getLastName().equalsIgnoreCase("")) {
                fullName.append(" ");
                fullName.append(customers.getLastName());
                initials.append(customers.getLastName().toUpperCase().charAt(0));
            }
            else {
                if (customers.getFirstName() != null && !customers.getFirstName().equalsIgnoreCase("")) {
                    if (customers.getFirstName().length() > 1) {
                        initials.append(customers.getFirstName().toUpperCase().charAt(1));
                    }
                }
            }
        }

        ComplaintTypeV1 type = complaintTypes
                .stream()
                .filter(i -> complaintsV1.getComplaintTypeId().equals(i.getComplaintTypeId()))
                .findFirst()
                .orElse(null);
        if (type != null) {
            complaintTypeName = type.getComplaintTypeName();
        }




        ComplaintResponseDto complaintResponseDto = new ComplaintResponseDto();

        complaintResponseDto.setComplaintId(complaintsV1.getComplaintId());
        complaintResponseDto.setCustomerId(customerId);
        complaintResponseDto.setCustomerName(fullName.toString());
        complaintResponseDto.setInitials(initials.toString());
        complaintResponseDto.setCustomerProfile(profilePic);
        complaintResponseDto.setHostelId(complaintsV1.getHostelId());
        complaintResponseDto.setComplaintDate(complaintsV1.getComplaintDate());
        complaintResponseDto.setDescription(complaintsV1.getDescription());
        complaintResponseDto.setAssigneeId(complaintsV1.getAssigneeId());
        complaintResponseDto.setAssignedDate(complaintsV1.getAssignedDate());
        complaintResponseDto.setComplaintTypeId(complaintsV1.getComplaintTypeId());
        complaintResponseDto.setComplaintTypeName(complaintTypeName);
        complaintResponseDto.setStatus(complaintsV1.getStatus());
        complaintResponseDto.setRoomName(roomName);
        complaintResponseDto.setFloorName(floorName);



        return complaintResponseDto;
    }
}

