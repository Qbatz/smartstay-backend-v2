package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.ComplaintListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.complaint.ComplaintResponse;
import com.smartstay.smartstay.dto.complaint.ComplaintResponseDto;
import com.smartstay.smartstay.payloads.complaints.*;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.responses.complaint.CommentResponse;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ComplaintsService {

    @Autowired
    RolesRepository rolesRepository;
    @Autowired
    HostelV1Repository hostelV1Repository;
    @Autowired
    FloorRepository floorRepository;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    ComplaintRepository complaintRepository;

    @Autowired
    ComplaintCommentsRepository commentsRepository;

    @Autowired
    CustomersRepository customersRepository;

    @Autowired
    BedsRepository bedsRepository;


    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;
    @Autowired
    private RolesService rolesService;

    public ResponseEntity<?> addComplaints(@RequestBody AddComplaints request) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);

        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_COMPLAINTS, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostelV1 = hostelV1Repository.findByHostelIdAndParentId(
                request.hostelId(),
                user.getParentId()
        );
        if (hostelV1 == null) {
            return new ResponseEntity<>("Hostel not found.", HttpStatus.BAD_REQUEST);
        }
        ComplaintsV1 complaint = new ComplaintsV1();
        Floors floors = null;
        if (request.floorId() != null) {
            floors = floorRepository.findByFloorIdAndHostelId(request.floorId(), request.hostelId());
            if (floors == null) {
                return new ResponseEntity<>("Floor not found in this hostel.", HttpStatus.BAD_REQUEST);
            }
            complaint.setFloorId(request.floorId());
        }else {
            complaint.setFloorId(0);
        }

        Rooms rooms = null;
        if (request.roomId() != null) {
            rooms = roomRepository.findByRoomIdAndParentIdAndHostelId(request.roomId(), user.getParentId(), request.hostelId());
            if (rooms == null) {
                return new ResponseEntity<>("Room not found in this hostel.", HttpStatus.BAD_REQUEST);
            }
            if (floors != null) {
                Rooms roomInFloor = roomRepository.findByRoomIdAndParentIdAndHostelIdAndFloorId(
                        request.roomId(), user.getParentId(), request.hostelId(), request.floorId());
                if (roomInFloor == null) {
                    return new ResponseEntity<>("This room is not linked to the given floor.", HttpStatus.BAD_REQUEST);
                }
            }
            complaint.setRoomId(request.roomId());
        }else {
            complaint.setRoomId(0);
        }

        if (request.bedId() != null){
            Beds bed = bedsRepository.findByBedIdAndParentIdAndHostelId(request.bedId(), user.getParentId(), request.hostelId());
            if (bed == null) {
                return new ResponseEntity<>("Bed not found in this hostel.", HttpStatus.BAD_REQUEST);
            }
            if (rooms != null) {
                Beds bedInRoom = bedsRepository.findByBedIdAndRoomIdAndParentId(
                        request.bedId(), rooms.getRoomId(), user.getParentId());
                if (bedInRoom == null) {
                    return new ResponseEntity<>("This bed is not linked to the given room.", HttpStatus.BAD_REQUEST);
                }
            }
            if (floors != null && rooms != null) {
                Beds bedInFloorRoom = bedsRepository.findByBedIdAndRoomIdAndParentId(
                        request.bedId(), rooms.getRoomId(), user.getParentId());
                if (bedInFloorRoom == null) {
                    return new ResponseEntity<>("This bed is not linked to the given floor and room combination.", HttpStatus.BAD_REQUEST);
                }
            }
            complaint.setBedId(request.bedId());

        }else {
            complaint.setBedId(0);
        }

        boolean customerExist = customersRepository.existsByHostelIdAndCustomerId(request.hostelId(), request.customerId());
         if (!customerExist){
            return new ResponseEntity<>("Customer not found.", HttpStatus.BAD_REQUEST);
        }

        complaint.setCustomerId(request.customerId());
        complaint.setComplaintTypeId(request.complaintTypeId());
        if (request.complaintDate() != null) {
            String formattedDate = request.complaintDate().replace("-", "/");
            complaint.setComplaintDate(Utils.stringToDate(formattedDate, Utils.DATE_FORMAT_YY));
        }
        complaint.setDescription(request.description());
        complaint.setCreatedAt(new Date());
        complaint.setUpdatedAt(new Date());
        complaint.setCreatedBy(user.getUserId());
        complaint.setParentId(user.getParentId());
        complaint.setHostelId(request.hostelId());
        complaint.setIsActive(true);

        complaintRepository.save(complaint);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }


    public ResponseEntity<?> addComplaintComments(@RequestBody AddComplaintComment request,int complaintId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);

        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_COMPLAINTS, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        ComplaintsV1 customerExist = complaintRepository.findByComplaintIdAndParentId(complaintId, user.getParentId());
        if (customerExist == null){
            return new ResponseEntity<>("Complaint not found.", HttpStatus.BAD_REQUEST);
        }

        ComplaintComments complaintComments = new ComplaintComments();
        complaintComments.setCommentDate(new Date());
        complaintComments.setComplaint(customerExist);
        complaintComments.setComment(request.message());
        complaintComments.setIsActive(true);
        complaintComments.setCreatedBy(user.getUserId());
        complaintComments.setUserName(user.getFirstName()+" "+user.getLastName());
        complaintComments.setCreatedAt(new Date());
        commentsRepository.save(complaintComments);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }


    public ResponseEntity<?> updateComplaints(int complaintId, UpdateComplaint request) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }

        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);

        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_COMPLAINTS, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        ComplaintsV1 complaint = complaintRepository.findByComplaintIdAndParentId(complaintId, user.getParentId());
        if (complaint == null) {
            return new ResponseEntity<>("Complaint not found.", HttpStatus.NOT_FOUND);
        }

        updateComplaint(complaint, request);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public ResponseEntity<?> updateComplaintStatus(int complaintId, UpdateStatus request) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }

        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);

        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_COMPLAINTS, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        ComplaintsV1 complaint = complaintRepository.findByComplaintIdAndParentId(complaintId, user.getParentId());
        if (complaint == null) {
            return new ResponseEntity<>("Complaint not found.", HttpStatus.NOT_FOUND);
        }

        complaint.setStatus(request.status());
        complaint.setUpdatedAt(new Date());
        complaintRepository.save(complaint);

        return new ResponseEntity<>(Utils.STATUS_UPDATED, HttpStatus.OK);
    }

    public ResponseEntity<?> getAllComplaints(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        Users users = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(users.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_COMPLAINTS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        List<Map<String, Object>> rawComplaints = complaintRepository.getAllComplaintsRaw(hostelId,user.getParentId());
        Map<String, Object> complaintsSummary = complaintRepository.getComplaintSummary(hostelId,user.getParentId());
        List<ComplaintComments> commentsList = complaintRepository.getCommentsByHostelId(hostelId,user.getParentId());
        ComplaintListMapper mapper = new ComplaintListMapper(commentsList,complaintsSummary);
        List<ComplaintResponse> responses = rawComplaints.stream()
                .map(mapper)
                .toList();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    public ResponseEntity<?> getComplaintById(int complaintId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        Users users = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(users.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_COMPLAINTS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        Map<String,Object> row = complaintRepository.getComplaintsWithType(complaintId, user.getParentId());
        if (row == null) {
            return new ResponseEntity<>("Complaint not found.", HttpStatus.BAD_REQUEST);
        }
        ComplaintResponseDto dto = new ComplaintResponseDto();
        dto.setComplaintId((Integer) row.get("complaintId"));
        dto.setCustomerId((String) row.get("customerId"));
        dto.setCustomerName((String) row.get("customerName"));
        dto.setCustomerProfile((String) row.get("customerProfile"));
        dto.setHostelId((String) row.get("hostelId"));
        dto.setFloorId((Integer) row.get("floorId"));
        dto.setFloorName((String) row.get("floorName"));
        dto.setRoomId((Integer) row.get("roomId"));
        dto.setRoomName((String) row.get("roomName"));
        dto.setBedId((Integer) row.get("bedId"));
        dto.setBedName((String) row.get("bedName"));
        dto.setComplaintDate(((Date) row.get("complaintDate")));
        dto.setDescription((String) row.get("description"));
        dto.setAssigneeName((String) row.get("assigneeName"));
        dto.setAssigneeId((String) row.get("assigneeId"));
        dto.setComplaintTypeId((Integer) row.get("complaintTypeId"));
        dto.setComplaintTypeName((String) row.get("complaintTypeName"));
        dto.setStatus((String) row.get("status"));
        dto.setCommentCount(((Number) row.get("commentCount")).intValue());
        // fetch comments separately
        List<Map<String, Object>> commentRows = complaintRepository.getCommentsByComplaintId(dto.getComplaintId());
        List<CommentResponse> comments = commentRows.stream()
                .map(c -> new CommentResponse(
                        (Integer) c.get("commentId"),
                        (Integer) c.get("complaintId"),
                        (String) c.get("commentText"),
                        (String) c.get("userName"),
                        (Date) c.get("commentDate"))
                )
                .toList();

        dto.setComments(comments);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    public ResponseEntity<?> assignUser(int complaintId, AssignUser request) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }

        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);

        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_COMPLAINTS, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        ComplaintsV1 complaint = complaintRepository.findByComplaintIdAndParentId(complaintId, user.getParentId());
        if (complaint == null) {
            return new ResponseEntity<>("Complaint not found.", HttpStatus.NOT_FOUND);
        }

        boolean users = usersService.existsByUserIdAndIsActiveTrueAndIsDeletedFalseAndParentId(request.userId(), user.getParentId());

        if (!users) {
            return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
        }
        complaint.setAssigneeId(request.userId());
        complaint.setAssignedDate(new Date());
        complaint.setUpdatedAt(new Date());
        complaintRepository.save(complaint);

        return new ResponseEntity<>(Utils.USER_ASSIGNED, HttpStatus.OK);
    }



    public ComplaintsV1 updateComplaint(ComplaintsV1 existingComplaint, UpdateComplaint request) {

        if (request.complaintDate() != null) {
            existingComplaint.setComplaintDate(Utils.stringToDate(request.complaintDate(), Utils.USER_INPUT_DATE_FORMAT));
        }
        if (request.description() != null) {
            existingComplaint.setDescription(request.description());
        }

        return complaintRepository.save(existingComplaint);
    }


}
