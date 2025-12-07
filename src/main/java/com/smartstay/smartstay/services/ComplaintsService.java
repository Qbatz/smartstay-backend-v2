package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.ComplaintListMapper;
import com.smartstay.smartstay.Wrappers.Notifications.NotificationListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.complaint.ComplaintResponse;
import com.smartstay.smartstay.dto.complaint.ComplaintResponseDto;
import com.smartstay.smartstay.dto.room.RoomInfo;
import com.smartstay.smartstay.ennum.ComplaintStatus;
import com.smartstay.smartstay.ennum.CustomerStatus;
import com.smartstay.smartstay.ennum.UserType;
import com.smartstay.smartstay.payloads.complaints.*;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.responses.complaint.CommentResponse;
import com.smartstay.smartstay.util.Utils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    UserHostelService userHostelService;
    @Autowired
    ComplaintCommentsRepository commentsRepository;
    @Autowired
    private CustomersService customersService;
    @Autowired
    BedsRepository bedsRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private BedsService bedsService;
    @Autowired
    private ComplaintTypeService complaintTypeService;
    @Autowired
    private CustomersBedHistoryService customersBedHistoryService;
    @Autowired
    private CustomerNotificationService customerNotificationService;

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
        List<String> currentStatus = Arrays.asList(
                CustomerStatus.CHECK_IN.name(),
                CustomerStatus.NOTICE.name()
        );

        boolean customerExist = customersService.existsByHostelIdAndCustomerIdAndStatusesIn(request.hostelId(), request.customerId(),currentStatus);
         if (!customerExist){
            return new ResponseEntity<>("Customer not found.", HttpStatus.BAD_REQUEST);
        }

        complaint.setCustomerId(request.customerId());
        complaint.setComplaintTypeId(request.complaintTypeId());
        if (request.complaintDate() != null) {
            String formattedDate = request.complaintDate().replace("/", "-");
            complaint.setComplaintDate(Utils.stringToDate(formattedDate, Utils.USER_INPUT_DATE_FORMAT));
        }
        complaint.setDescription(request.description());
        complaint.setCreatedAt(new Date());
        complaint.setUpdatedAt(new Date());
        complaint.setCreatedBy(user.getUserId());
        complaint.setParentId(user.getParentId());
        complaint.setHostelId(request.hostelId());
        complaint.setIsActive(true);
        complaint.setStatus(ComplaintStatus.PENDING.name());
        complaint.setIsDeleted(false);

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

        ComplaintsV1 complaintExist = complaintRepository.findByComplaintIdAndParentId(complaintId, user.getParentId());
        if (complaintExist == null){
            return new ResponseEntity<>("Complaint not found.", HttpStatus.BAD_REQUEST);
        }

        ComplaintComments complaintComments = new ComplaintComments();
        complaintComments.setCommentDate(new Date());
        complaintComments.setComplaint(complaintExist);
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

    public ResponseEntity<?> getAllComplaints(String hostelId,
                                              String customerName,
                                              String status,
                                              String startDate,
                                              String endDate
                                              ) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_COMPLAINTS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.BAD_REQUEST);
        }

        List<ComplaintsV1> listComplaints = complaintRepository.findByHostelIdOrderByComplaintDateDesc(hostelId);
        List<String> customerIds = listComplaints
                .stream()
                .map(ComplaintsV1::getCustomerId)
                .toList();
        List<String> assignes = listComplaints
                .stream()
                .map(ComplaintsV1::getAssigneeId)
                .toList();
        List<Integer> roomIds = listComplaints
                .stream()
                .filter(i -> i.getRoomId() != null)
                .map(ComplaintsV1::getRoomId)
                .toList();
        List<Integer> complaintTypeIds = listComplaints.stream()
                .map(ComplaintsV1::getComplaintTypeId)
                .toList();
        List<ComplaintTypeV1> listComplaintTypes = complaintTypeService.getComplaintTypesById(complaintTypeIds);
        List<Users> listUsers = usersService.findByListOfUserIds(assignes);

        List<Customers> listCustomers = customersService.getCustomerDetails(customerIds);
        List<CustomersBedHistory> listCustomersBedHistory = customersBedHistoryService.getCurrentBedHistoryByCustomerIds(customerIds);

        List<BedDetails> listBedDetails;
        if (listCustomersBedHistory != null) {
            List<Integer> bedIds = listCustomersBedHistory
                    .stream()
                    .map(CustomersBedHistory::getBedId)
                    .toList();
            listBedDetails = bedsService.getBedDetails(bedIds);

        } else {
            listBedDetails = new ArrayList<>();
        }


        Date start = listComplaints.stream()
                .map(ComplaintsV1::getCreatedAt)
                .filter(Objects::nonNull)
                .min(Date::compareTo)
                .orElse(null);
        Date end = listComplaints.stream()
                .map(ComplaintsV1::getCreatedAt)
                .filter(Objects::nonNull)
                .max(Date::compareTo)
                .orElse(null);

        String sDate = null;
        String eDate = null;

        if (start != null) {
            sDate = Utils.dateToString(start);
        }
        if (end != null) {
            eDate = Utils.dateToString(end);
        }

        List<ComplaintResponseDto> listComplaintsResponse = listComplaints
                .stream()
                .map(i -> new ComplaintListMapper(listCustomers, listComplaintTypes, listBedDetails, listCustomersBedHistory, listUsers).apply(i))
                .toList();

        ComplaintResponse complaintResponse = new ComplaintResponse(hostelId, sDate, eDate, listComplaints.size(), listComplaintsResponse);

        return new ResponseEntity<>(complaintResponse, HttpStatus.OK);
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
        List<String> currentStatus = Arrays.asList(
                CustomerStatus.CHECK_IN.name(),
                CustomerStatus.NOTICE.name()
        );
        Map<String,Object> row = complaintRepository.getComplaintsWithType(complaintId, user.getParentId(),currentStatus);
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
        dto.setAssignedDate(((Date) row.get("assignedDate")));
        dto.setDescription((String) row.get("description"));
        dto.setAssigneeName((String) row.get("assigneeName"));
        dto.setAssigneeId((String) row.get("assigneeId"));
        dto.setComplaintTypeId((Integer) row.get("complaintTypeId"));
        dto.setComplaintTypeName((String) row.get("complaintTypeName"));
        dto.setStatus((String) row.get("status"));
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

//        dto.setComments(comments);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    public ResponseEntity<?> assignUser(int complaintId, AssignUser request) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
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
            return new ResponseEntity<>(Utils.COMPLAINT_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }
        Customers customers = customersService.getCustomerInformation(complaint.getCustomerId());

        Users users = usersService.existsByUserIdAndIsActiveTrueAndIsDeletedFalseAndParentId(request.userId(), user.getParentId());

        if (users == null) {
            return new ResponseEntity<>(Utils.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }
        complaint.setAssigneeId(request.userId());
        complaint.setAssignedDate(new Date());
        complaint.setStatus(ComplaintStatus.ASSIGNED.name());
        complaint.setUpdatedAt(new Date());

        List<ComplaintUpdates> listComplaintUpdates = complaint.getComplaintUpdates();
        if (listComplaintUpdates == null) {
            listComplaintUpdates = new ArrayList<>();
        }

        ComplaintUpdates complaintUpdates = new ComplaintUpdates();
        complaintUpdates.setComplaint(complaint);
        complaintUpdates.setStatus(ComplaintStatus.ASSIGNED.name());
        complaintUpdates.setUserType(UserType.ADMIN.name());
        complaintUpdates.setCreatedAt(new Date());
        complaintUpdates.setUpdatedBy(authentication.getName());

        listComplaintUpdates.add(complaintUpdates);

        complaintRepository.save(complaint);
        StringBuilder userName = new StringBuilder();
        userName.append(user.getFirstName());
        if (user.getLastName() != null && !user.getLastName().equalsIgnoreCase("")) {
            userName.append(" ");
            userName.append(user.getLastName());
        }

        //send a push notification
        customerNotificationService.addComplainUpdateStatus(complaint, userName.toString(), customers.getXuid());

        return new ResponseEntity<>(Utils.USER_ASSIGNED, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteComplaint(Integer complaintId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);

        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_COMPLAINTS, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        ComplaintsV1 complaint = complaintRepository.findByComplaintIdAndParentId(complaintId, user.getParentId());
        if (complaint == null) {
            return new ResponseEntity<>("Complaint not found.", HttpStatus.NOT_FOUND);
        }
        complaint.setUpdatedAt(new Date());
        complaint.setIsDeleted(true);
        complaint.setIsActive(false);
        complaintRepository.save(complaint);
        return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);
    }


    public ComplaintsV1 updateComplaint(ComplaintsV1 existingComplaint, UpdateComplaint request) {

        if (request.complaintDate() != null) {
            existingComplaint.setComplaintDate(Utils.stringToDate(request.complaintDate().replace("/","-"), Utils.USER_INPUT_DATE_FORMAT));
        }
        if (request.description() != null) {
            existingComplaint.setDescription(request.description());
        }

        return complaintRepository.save(existingComplaint);
    }


}
