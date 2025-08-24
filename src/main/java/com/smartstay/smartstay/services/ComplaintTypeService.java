package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.ComplaintTypeV1;
import com.smartstay.smartstay.dao.HostelV1;
import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.payloads.complaints.AddComplaintType;
import com.smartstay.smartstay.payloads.complaints.UpdateComplaintType;
import com.smartstay.smartstay.repositories.ComplaintTypeV1Repository;
import com.smartstay.smartstay.repositories.HostelV1Repository;
import com.smartstay.smartstay.repositories.RolesRepository;
import com.smartstay.smartstay.responses.complaint.ComplaintTypeResponse;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ComplaintTypeService {

    @Autowired
    RolesRepository rolesRepository;
    @Autowired
    HostelV1Repository hostelV1Repository;

    @Autowired
    ComplaintTypeV1Repository complaintTypeV1Repository;

    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;
    @Autowired
    private RolesService rolesService;


    public ResponseEntity<?> addComplaintType(AddComplaintType request) {
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

        ComplaintTypeV1 complaintTypeV1 = new ComplaintTypeV1();
        complaintTypeV1.setComplaintTypeName(request.complaintTypeName());
        complaintTypeV1.setCreatedAt(new Date());
        complaintTypeV1.setUpdatedAt(new Date());
        complaintTypeV1.setCreatedBy(user.getUserId());
        complaintTypeV1.setParentId(user.getParentId());
        complaintTypeV1.setHostelId(request.hostelId());
        complaintTypeV1.setIsActive(true);

        complaintTypeV1Repository.save(complaintTypeV1);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public ResponseEntity<?> updateComplaintType(UpdateComplaintType request, int complaintTypeId) {
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

        ComplaintTypeV1 complaintTypeV1 = complaintTypeV1Repository.findById(complaintTypeId).orElse(null);
        if (complaintTypeV1 == null) {
            return new ResponseEntity<>("Complaint type not found.", HttpStatus.NOT_FOUND);
        }

        HostelV1 hostelV1 = hostelV1Repository.findByHostelIdAndParentId(complaintTypeV1.getHostelId(), user.getParentId());
        if (hostelV1 == null) {
            return new ResponseEntity<>("Hostel not found.", HttpStatus.BAD_REQUEST);
        }

        if (request.complaintTypeName() != null && !request.complaintTypeName().isEmpty()) {
            complaintTypeV1.setComplaintTypeName(request.complaintTypeName());
        }
        if (request.isActive() != null) {
            complaintTypeV1.setIsActive(request.isActive());
        }

        complaintTypeV1.setUpdatedAt(new Date());

        complaintTypeV1Repository.save(complaintTypeV1);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }


    public ResponseEntity<?> deleteComplaintType(Integer complaintTypeId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.INVALID_USER, HttpStatus.UNAUTHORIZED);
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

        ComplaintTypeV1 complaintTypeV1 = complaintTypeV1Repository.findById(complaintTypeId).orElse(null);

        if (complaintTypeV1 == null || !complaintTypeV1.getParentId().equals(user.getParentId())) {
            return new ResponseEntity<>("Complaint type not found.", HttpStatus.NOT_FOUND);
        }

        complaintTypeV1Repository.delete(complaintTypeV1);

        return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);
    }

    public ResponseEntity<?> getAllComplaintTypes(String hostelId) {
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
        List<ComplaintTypeResponse> complaintTypeResponses = complaintTypeV1Repository.getAllComplaintsType(hostelId);
        return new ResponseEntity<>(complaintTypeResponses, HttpStatus.OK);
    }
}
