package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.payloads.complaints.AddComplaints;
import com.smartstay.smartstay.payloads.complaints.UpdateComplaint;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.responses.complaint.ComplaintResponse;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.List;

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
    CustomersRepository customersRepository;


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

        Floors floors = floorRepository.findByFloorIdAndHostelId(request.floorId(), request.hostelId());
        boolean customerExist = customersRepository.existsByHostelIdAndCustomerId(request.hostelId(), request.customerId());
        Rooms rooms = roomRepository.findByRoomIdAndParentIdAndHostelId(request.roomId(), user.getParentId(), request.hostelId());
        if (floors == null) {
            return new ResponseEntity<>("Floor not found.", HttpStatus.BAD_REQUEST);
        }else if (!customerExist){
            return new ResponseEntity<>("Customer not found.", HttpStatus.BAD_REQUEST);
        }else if (rooms == null) {
            return new ResponseEntity<>("Room not found.", HttpStatus.BAD_REQUEST);
        }

        ComplaintsV1 complaint = new ComplaintsV1();
        complaint.setCustomerId(request.customerId());
        complaint.setComplaintTypeId(request.complaintTypeId());
        complaint.setFloorId(request.floorId());
        complaint.setRoomId(request.roomId());
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

        if (request.floorId() !=null){
            Floors floors = floorRepository.findByFloorIdAndHostelId(request.floorId(), complaint.getHostelId());
            if (floors == null) {
                return new ResponseEntity<>("Floor not found.", HttpStatus.BAD_REQUEST);
            }
        }

        if (request.roomId() !=null){
            Rooms rooms = roomRepository.findByRoomIdAndParentIdAndHostelId(
                    request.roomId(),
                    user.getParentId(),
                    complaint.getHostelId()
            );
            if (rooms == null) {
                return new ResponseEntity<>("Room not found.", HttpStatus.BAD_REQUEST);
            }
        }

        updateComplaint(complaint, request);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
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
        List<ComplaintResponse> complaintResponses = complaintRepository.getAllComplaintsWithType(hostelId);
        return new ResponseEntity<>(complaintResponses, HttpStatus.OK);
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
        ComplaintResponse complaintResponse = complaintRepository.getComplaintsWithType(complaintId, user.getParentId());
        if (complaintResponse == null) {
            return new ResponseEntity<>("Complaint not found.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(complaintResponse, HttpStatus.OK);
    }

    public ComplaintsV1 updateComplaint(ComplaintsV1 existingComplaint, UpdateComplaint request) {

        if (request.customerId() != null) {
            existingComplaint.setCustomerId(request.customerId());
        }
        if (request.complaintTypeId() != null) {
            existingComplaint.setComplaintTypeId(request.complaintTypeId());
        }
        if (request.floorId() != null) {
            existingComplaint.setFloorId(request.floorId());
        }
        if (request.roomId() != null) {
            existingComplaint.setRoomId(request.roomId());
        }
        if (request.complaintDate() != null) {
            existingComplaint.setComplaintDate(Utils.stringToDate(request.complaintDate(), Utils.DATE_FORMAT_YY));
        }
        if (request.description() != null) {
            existingComplaint.setDescription(request.description());
        }

        return complaintRepository.save(existingComplaint);
    }


}
