package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.ComplaintListMapper;
import com.smartstay.smartstay.Wrappers.complaints.ComplaintUpdatesMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.complaint.ComplaintResponse;
import com.smartstay.smartstay.dto.complaint.ComplaintResponseDto;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.payloads.complaints.*;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.responses.complaint.CommentResponse;
import com.smartstay.smartstay.responses.complaint.ComplaintUpdatesList;
import com.smartstay.smartstay.responses.complaint.ComplaintsUpdates;
import com.smartstay.smartstay.util.Utils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

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
    BedsRepository bedsRepository;
    @Autowired
    private CustomersService customersService;
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
    @Autowired
    private ComplaintCommentsRepository complaintCommentsRepository;

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

        HostelV1 hostelV1 = hostelV1Repository.findByHostelIdAndParentId(request.hostelId(), user.getParentId());
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
        } else {
            complaint.setFloorId(0);
        }

        Rooms rooms = null;
        if (request.roomId() != null) {
            rooms = roomRepository.findByRoomIdAndParentIdAndHostelId(request.roomId(), user.getParentId(), request.hostelId());
            if (rooms == null) {
                return new ResponseEntity<>("Room not found in this hostel.", HttpStatus.BAD_REQUEST);
            }
            if (floors != null) {
                Rooms roomInFloor = roomRepository.findByRoomIdAndParentIdAndHostelIdAndFloorId(request.roomId(), user.getParentId(), request.hostelId(), request.floorId());
                if (roomInFloor == null) {
                    return new ResponseEntity<>("This room is not linked to the given floor.", HttpStatus.BAD_REQUEST);
                }
            }
            complaint.setRoomId(request.roomId());
        } else {
            complaint.setRoomId(0);
        }

        if (request.bedId() != null) {
            Beds bed = bedsRepository.findByBedIdAndParentIdAndHostelId(request.bedId(), user.getParentId(), request.hostelId());
            if (bed == null) {
                return new ResponseEntity<>("Bed not found in this hostel.", HttpStatus.BAD_REQUEST);
            }
            if (rooms != null) {
                Beds bedInRoom = bedsRepository.findByBedIdAndRoomIdAndParentId(request.bedId(), rooms.getRoomId(), user.getParentId());
                if (bedInRoom == null) {
                    return new ResponseEntity<>("This bed is not linked to the given room.", HttpStatus.BAD_REQUEST);
                }
            }
            if (floors != null && rooms != null) {
                Beds bedInFloorRoom = bedsRepository.findByBedIdAndRoomIdAndParentId(request.bedId(), rooms.getRoomId(), user.getParentId());
                if (bedInFloorRoom == null) {
                    return new ResponseEntity<>("This bed is not linked to the given floor and room combination.", HttpStatus.BAD_REQUEST);
                }
            }
            complaint.setBedId(request.bedId());

        } else {
            complaint.setBedId(0);
        }
        List<String> currentStatus = Arrays.asList(CustomerStatus.CHECK_IN.name(), CustomerStatus.NOTICE.name());

        boolean customerExist = customersService.existsByHostelIdAndCustomerIdAndStatusesIn(request.hostelId(), request.customerId(), currentStatus);
        if (!customerExist) {
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

        ComplaintsV1 complaintsV1 = complaintRepository.save(complaint);
        List<String> tenantId = new ArrayList<>();
        tenantId.add(request.customerId());
        usersService.addUserLog(hostelV1.getHostelId(), String.valueOf(complaintsV1.getComplaintId()), ActivitySource.COMPLAINTS, ActivitySourceType.CREATE, user, tenantId);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }


    public ResponseEntity<?> addComplaintComments(@RequestBody AddComplaintComment request, int complaintId) {
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

        ComplaintsV1 complaintExist = complaintRepository.findByComplaintIdAndParentId(complaintId, user.getParentId());
        if (complaintExist == null) {
            return new ResponseEntity<>(Utils.INVALID, HttpStatus.BAD_REQUEST);
        }

        ComplaintComments complaintComments = new ComplaintComments();
        complaintComments.setCommentDate(new Date());
        complaintComments.setComplaint(complaintExist);
        complaintComments.setComment(request.message());
        complaintComments.setIsActive(true);
        complaintComments.setCreatedBy(user.getUserId());
        complaintComments.setUserType(UserType.ADMIN.name());
        complaintComments.setComplaintStatus(complaintExist.getStatus());
        complaintComments.setUserName(user.getFirstName() + " " + user.getLastName());
        complaintComments.setCreatedAt(new Date());
        commentsRepository.save(complaintComments);

        Customers customers = customersService.getCustomerInformation(complaintExist.getCustomerId());
        if (customers != null) {
            customerNotificationService.sendNotifications(customers.getXuid(), complaintExist, request.message(), user.getFirstName() + " " + user.getLastName());
        }

        usersService.addUserLog(rolesV1.getHostelId(), String.valueOf(complaintId), ActivitySource.COMMENTS, ActivitySourceType.CREATE, user);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public ResponseEntity<?> updateComplaints(int complaintId, UpdateComplaint request) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);

        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_COMPLAINTS, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        ComplaintsV1 complaint = complaintRepository.findByComplaintIdAndParentId(complaintId, user.getParentId());
        if (complaint == null) {
            return new ResponseEntity<>(Utils.COMPLAINT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        updateComplaint(complaint, request);
        usersService.addUserLog(complaint.getHostelId(), String.valueOf(complaintId), ActivitySource.COMPLAINTS, ActivitySourceType.UPDATE, user);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public ResponseEntity<?> updateComplaintStatus(int complaintId, UpdateStatus request) {
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
            return new ResponseEntity<>("Complaint not found.", HttpStatus.NOT_FOUND);
        }

        StringBuilder fullName = new StringBuilder();
        if (user.getFirstName() != null) {
            fullName.append(user.getFirstName());
        }
        if (user.getLastName() != null && !user.getLastName().trim().equalsIgnoreCase("")) {
            fullName.append(" ");
            fullName.append(user.getLastName());
        }

        Customers customers = customersService.getCustomerInformation(complaint.getCustomerId());
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }

        List<ComplaintUpdates> listComplaintUpdates = complaint.getComplaintUpdates();
        if (listComplaintUpdates == null) {
            listComplaintUpdates = new ArrayList<>();
        }
        String status = null;
        if (request.status().equalsIgnoreCase(ComplaintStatus.PENDING.name())) {
            status = ComplaintStatus.PENDING.name();
        } else if (request.status().equalsIgnoreCase(ComplaintStatus.RESOLVED.name())) {
            status = ComplaintStatus.RESOLVED.name();
        } else if (request.status().equalsIgnoreCase(ComplaintStatus.IN_PROGRESS.name())) {
            status = ComplaintStatus.IN_PROGRESS.name();
        }

        ComplaintUpdates cu = new ComplaintUpdates();
        cu.setUpdatedBy(userId);
        cu.setComplaint(complaint);
        cu.setUserType(UserType.ADMIN.name());
        cu.setCreatedAt(new Date());
        cu.setStatus(status);
        listComplaintUpdates.add(cu);

        complaint.setStatus(request.status());
        complaint.setUpdatedAt(new Date());
        complaintRepository.save(complaint);

        ComplaintTypeV1 complaintTypeV1 = complaintTypeService.getComplaintType(complaint.getComplaintTypeId());
        String complaintTypeName = null;
        if (complaintTypeV1 != null) {
            complaintTypeName = complaintTypeV1.getComplaintTypeName();
        }

        List<String> tenantId = new ArrayList<>();
        tenantId.add(complaint.getCustomerId());
        customerNotificationService.addComplainUpdateStatus(complaint, fullName.toString(), customers.getXuid(), request.status(), complaintTypeName);
        usersService.addUserLog(complaint.getHostelId(), String.valueOf(complaintId), ActivitySource.COMPLAINTS, ActivitySourceType.ASSIGN, user, tenantId);
        return new ResponseEntity<>(Utils.STATUS_UPDATED, HttpStatus.OK);
    }

    public ResponseEntity<?> getAllComplaints(String hostelId, String customerName, String status, String startDate, String endDate) {
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
        List<String> customerIds = listComplaints.stream().map(ComplaintsV1::getCustomerId).toList();
        List<String> assignes = listComplaints.stream().map(ComplaintsV1::getAssigneeId).toList();
        List<Integer> roomIds = listComplaints.stream().filter(i -> i.getRoomId() != null).map(ComplaintsV1::getRoomId).toList();
        List<Integer> complaintTypeIds = listComplaints.stream().map(ComplaintsV1::getComplaintTypeId).toList();
        List<ComplaintTypeV1> listComplaintTypes = complaintTypeService.getComplaintTypesById(complaintTypeIds);
        List<Users> listUsers = usersService.findByListOfUserIds(assignes);

        List<Customers> listCustomers = customersService.getCustomerDetails(customerIds);
        List<CustomersBedHistory> listCustomersBedHistory = customersBedHistoryService.getCurrentBedHistoryByCustomerIds(customerIds);

        List<BedDetails> listBedDetails;
        if (listCustomersBedHistory != null) {
            List<Integer> bedIds = listCustomersBedHistory.stream().map(CustomersBedHistory::getBedId).toList();
            listBedDetails = bedsService.getBedDetails(bedIds);

        } else {
            listBedDetails = new ArrayList<>();
        }


        Date start = listComplaints.stream().map(ComplaintsV1::getCreatedAt).filter(Objects::nonNull).min(Date::compareTo).orElse(null);
        Date end = listComplaints.stream().map(ComplaintsV1::getCreatedAt).filter(Objects::nonNull).max(Date::compareTo).orElse(null);

        String sDate = null;
        String eDate = null;

        if (start != null) {
            sDate = Utils.dateToString(start);
        }
        if (end != null) {
            eDate = Utils.dateToString(end);
        }

        List<ComplaintResponseDto> listComplaintsResponse = listComplaints.stream().map(i -> new ComplaintListMapper(listCustomers, listComplaintTypes, listBedDetails, listCustomersBedHistory, listUsers).apply(i)).toList();

        ComplaintResponse complaintResponse = new ComplaintResponse(hostelId, sDate, eDate, listComplaints.size(), listComplaintsResponse);

        return new ResponseEntity<>(complaintResponse, HttpStatus.OK);
    }


    public ResponseEntity<?> getComplaintById(int complaintId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
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
        List<String> currentStatus = Arrays.asList(CustomerStatus.CHECK_IN.name(), CustomerStatus.NOTICE.name());
        Map<String, Object> row = complaintRepository.getComplaintsWithType(complaintId, user.getParentId(), currentStatus);
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

        List<ComplaintComments> listComplaintComments = complaintCommentsRepository.findByComplaint_ComplaintId(complaintId);
        if (listComplaintComments != null && !listComplaintComments.isEmpty()) {
            List<String> adminUserIds = listComplaintComments.stream().filter(i -> !i.getUserType().equalsIgnoreCase(UserType.TENANT.name())).map(ComplaintComments::getCreatedBy).toList();

            List<String> tenantUserIds = listComplaintComments.stream().filter(i -> i.getUserType().equalsIgnoreCase(UserType.TENANT.name())).map(ComplaintComments::getCreatedBy).toList();

            List<Users> adminUsers = usersService.findAllUsersFromUserId(adminUserIds);
            List<Customers> tenants = customersService.getCustomerDetails(tenantUserIds);

            List<CommentResponse> commentsNew = listComplaintComments.stream().map(i -> {
                StringBuilder initials = new StringBuilder();
                String profilePic = null;
                if (i.getUserType().equalsIgnoreCase(UserType.TENANT.name())) {
                    if (!tenants.isEmpty()) {
                        Customers cus = tenants.stream().filter(tnt -> tnt.getCustomerId().equalsIgnoreCase(i.getCreatedBy())).findFirst().orElse(null);
                        if (cus != null) {
                            profilePic = cus.getProfilePic();
                            if (cus.getFirstName() != null) {
                                initials.append(cus.getFirstName().trim().toUpperCase().charAt(0));
                            }
                            if (cus.getLastName() != null && !cus.getLastName().trim().equalsIgnoreCase("")) {
                                initials.append(cus.getLastName().trim().toUpperCase().charAt(0));
                            } else if (cus.getFirstName() != null) {
                                if (cus.getFirstName().length() > 1) {
                                    initials.append(cus.getFirstName().trim().toUpperCase().charAt(1));
                                }
                            }

                        }
                    }
                } else {
                    if (!adminUsers.isEmpty()) {
                        Users admUsr = adminUsers.stream().filter(tnt -> tnt.getUserId().equalsIgnoreCase(i.getCreatedBy())).findFirst().orElse(null);
                        if (admUsr != null) {
                            profilePic = admUsr.getProfileUrl();
                            if (admUsr.getFirstName() != null) {
                                initials.append(admUsr.getFirstName().trim().toUpperCase().charAt(0));
                            }
                            if (admUsr.getLastName() != null && !admUsr.getLastName().trim().equalsIgnoreCase("")) {
                                initials.append(admUsr.getLastName().trim().toUpperCase().charAt(0));
                            } else if (admUsr.getFirstName() != null) {
                                if (admUsr.getFirstName().length() > 1) {
                                    initials.append(admUsr.getFirstName().trim().toUpperCase().charAt(1));
                                }
                            }
                        }
                    }
                }
                return new CommentResponse(i.getCommentId(), i.getComplaint().getComplaintId(), i.getComment(), i.getUserName(), i.getCreatedAt(), initials.toString(), profilePic);
            }).toList();
            dto.setComments(commentsNew);
        }
        // fetch comments separately
        // List<Map<String, Object>> commentRows =
        // complaintRepository.getCommentsByComplaintId(dto.getComplaintId());
        //
        // List<CommentResponse> comments = commentRows.stream()
        // .map(c -> {
        // StringBuilder intials = new StringBuilder();
        // String profilePic = null;
        // String userName = (String)c.get("userName");
        //
        // return new CommentResponse(
        // (Integer) c.get("commentId"),
        // (Integer) c.get("complaintId"),
        // (String) c.get("commentText"),
        // (String) c.get("userName"),
        // (Date) c.get("commentDate"),
        // intials.toString(),
        // profilePic);
        // }
        // )
        // .toList();

        // dto.setComments(comments);
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
        ComplaintTypeV1 complaintTypeV1 = complaintTypeService.getComplaintType(complaint.getComplaintTypeId());
        String complaintTypeName = null;
        if (complaintTypeV1 != null) {
            complaintTypeName = complaintTypeV1.getComplaintTypeName();
        }

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
        complaintUpdates.setAssignedTo(users.getUserId());
        complaintUpdates.setUpdatedBy(authentication.getName());

        listComplaintUpdates.add(complaintUpdates);
        complaint.setComplaintUpdates(listComplaintUpdates);

        complaintRepository.save(complaint);
        StringBuilder userName = new StringBuilder();
        userName.append(users.getFirstName());
        if (users.getLastName() != null && !users.getLastName().equalsIgnoreCase("")) {
            userName.append(" ");
            userName.append(users.getLastName());
        }

        //send a push notification
        customerNotificationService.addComplainUpdateStatus(complaint, userName.toString(), customers.getXuid(), ComplaintStatus.ASSIGNED.name(), complaintTypeName);

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
        usersService.addUserLog(complaint.getHostelId(), String.valueOf(complaintId), ActivitySource.COMPLAINTS, ActivitySourceType.DELETE, user);
        return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);
    }


    public ComplaintsV1 updateComplaint(ComplaintsV1 existingComplaint, UpdateComplaint request) {

        if (request.complaintDate() != null) {
            existingComplaint.setComplaintDate(Utils.stringToDate(request.complaintDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));
        }
        if (request.description() != null) {
            existingComplaint.setDescription(request.description());
        }

        return complaintRepository.save(existingComplaint);
    }


    public ResponseEntity<?> getComplaintUpdates(String hostelId, Integer complaintId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_COMPLAINTS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        ComplaintsV1 complaintsV1 = complaintRepository.findByComplaintIdAndParentId(complaintId, users.getParentId());
        if (complaintsV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_COMPLAINT_ID_PASSED, HttpStatus.BAD_REQUEST);
        }

        if (!hostelId.equalsIgnoreCase(complaintsV1.getHostelId())) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        List<Integer> complaintType = new ArrayList<>();
        complaintType.add(complaintsV1.getComplaintTypeId());


        List<ComplaintTypeV1> complaintTypeV1s = complaintTypeService.getComplaintTypesById(complaintType);
        String complaintTypeStr;
        if (complaintTypeV1s != null) {
            complaintTypeStr = complaintTypeV1s.get(0).getComplaintTypeName();
        } else {
            complaintTypeStr = null;
        }

        List<ComplaintUpdates> listComplaintUpdates = complaintsV1.getComplaintUpdates();
        List<ComplaintComments> listComplaintComments = complaintsV1.getComplaintComments();
        List<String> updatedByAdminUsers = new ArrayList<>(listComplaintUpdates.stream().filter(i -> !i.getUserType().equalsIgnoreCase(UserType.TENANT.name())).map(ComplaintUpdates::getUpdatedBy).toList());
        List<String> updatedByTenantUsers = new ArrayList<>(listComplaintUpdates.stream().filter(i -> i.getUserType().equalsIgnoreCase(UserType.TENANT.name())).map(ComplaintUpdates::getUpdatedBy).toList());
        updatedByAdminUsers.addAll(listComplaintComments.stream().filter(i -> !i.getUserType().equalsIgnoreCase(UserType.TENANT.name())).map(ComplaintComments::getCreatedBy).toList());
        updatedByTenantUsers.addAll(listComplaintComments.stream().filter(i -> i.getUserType().equalsIgnoreCase(UserType.TENANT.name())).map(ComplaintComments::getCreatedBy).toList());

        List<Users> adminUsers = usersService.findByListOfUserIds(updatedByAdminUsers);
        List<Customers> tenantUsers = customersService.getCustomerDetails(updatedByTenantUsers);

        List<String> assignedUsersIds = listComplaintUpdates.stream().filter(i -> i.getStatus().equalsIgnoreCase(ComplaintStatus.ASSIGNED.name())).map(ComplaintUpdates::getAssignedTo).toList();
        List<Users> assignedUsers = usersService.findByListOfUserIds(assignedUsersIds);

        List<ComplaintUpdatesList> listComments = listComplaintUpdates.stream().map(i -> new ComplaintUpdatesMapper(tenantUsers, adminUsers, listComplaintComments, complaintsV1.getDescription(), assignedUsers, complaintTypeStr).apply(i)).toList();
        ComplaintsUpdates updates = new ComplaintsUpdates(complaintId, listComments);


        return new ResponseEntity<>(updates, HttpStatus.OK);

    }

    public int countByHostelIdAndDateRange(String hostelId, Date startDate, Date endDate) {
        return complaintRepository.countByHostelIdAndDateRange(hostelId, startDate, endDate);
    }

    public int countActiveByHostelIdAndDateRange(String hostelId, List<String> statuses, Date startDate, Date endDate) {
        return complaintRepository.countActiveByHostelIdAndDateRange(hostelId, statuses, startDate, endDate);
    }


    public Page<ComplaintsV1> getFilteredComplaints(String hostelId, Date startDate, Date endDate, List<String> status, List<String> raisedBy, List<Integer> complaintTypeIds, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "complaintDate"));

        Specification<ComplaintsV1> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("hostelId"), hostelId));
            predicates.add(cb.equal(root.get("isActive"), true));
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("complaintDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("complaintDate"), endDate));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(root.get("status").in(status));
            }
            if (raisedBy != null && !raisedBy.isEmpty()) {
                predicates.add(root.get("customerId").in(raisedBy));
            }
            if (complaintTypeIds != null && !complaintTypeIds.isEmpty()) {
                predicates.add(root.get("complaintTypeId").in(complaintTypeIds));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return complaintRepository.findAll(spec, pageable);
    }

    public Map<String, Long> getComplaintSummary(String hostelId, Date startDate, Date endDate, List<String> status, List<String> raisedBy, List<Integer> complaintTypeIds) {

        org.springframework.data.jpa.domain.Specification<ComplaintsV1> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("hostelId"), hostelId));
            predicates.add(cb.equal(root.get("isActive"), true));
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("complaintDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("complaintDate"), endDate));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(root.get("status").in(status));
            }
            if (raisedBy != null && !raisedBy.isEmpty()) {
                predicates.add(root.get("customerId").in(raisedBy));
            }
            if (complaintTypeIds != null && !complaintTypeIds.isEmpty()) {
                predicates.add(root.get("complaintTypeId").in(complaintTypeIds));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        List<ComplaintsV1> allFiltered = complaintRepository.findAll(spec);

        Map<String, Long> summary = new HashMap<>();
        long total = allFiltered.size();
        long resolved = allFiltered.stream().filter(c -> ComplaintStatus.RESOLVED.name().equalsIgnoreCase(c.getStatus())).count();
        long inprogress = allFiltered.stream().filter(c -> ComplaintStatus.IN_PROGRESS.name().equalsIgnoreCase(c.getStatus()) || ComplaintStatus.ASSIGNED.name().equalsIgnoreCase(c.getStatus()) || ComplaintStatus.OPENED.name().equalsIgnoreCase(c.getStatus())).count();

        summary.put("total", total);
        summary.put("resolved", resolved);
        summary.put("inprogress", inprogress);

        return summary;
    }

    public List<String> getDistinctCustomerIdsByHostelId(String hostelId) {
        return complaintRepository.findDistinctCustomerIdsByHostelId(hostelId);
    }
}
