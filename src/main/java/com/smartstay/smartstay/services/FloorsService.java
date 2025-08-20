package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.FloorsMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.Floors;
import com.smartstay.smartstay.dao.HostelV1;
import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.payloads.floor.AddFloors;
import com.smartstay.smartstay.payloads.floor.UpdateFloor;
import com.smartstay.smartstay.repositories.FloorRepository;
import com.smartstay.smartstay.repositories.HostelV1Repository;
import com.smartstay.smartstay.repositories.RolesRepository;
import com.smartstay.smartstay.responses.floors.FloorsResponse;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class FloorsService {

    @Autowired
    RolesRepository rolesRepository;

    @Autowired
    HostelV1Repository hostelV1Repository;

    @Autowired
    FloorRepository floorRepository;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;

    public ResponseEntity<?> getAllFloors(String hostelId) {
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
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        List<Floors> listFloor = floorRepository.findAllByHostelIdAndParentId(hostelId,user.getParentId());
        List<FloorsResponse> floorsResponses = listFloor.stream().map(item -> new FloorsMapper().apply(item)).toList();
        return new ResponseEntity<>(floorsResponses, HttpStatus.OK);
    }

    public List<Floors> getFloorByHostelID(String hostelId, String parentID){
        return floorRepository.findAllByHostelIdAndParentId(hostelId,parentID);
    }

    public ResponseEntity<?> getFloorById(Integer id) {
        if (id == null || id == 0) {
            return new ResponseEntity<>(Utils.INVALID, HttpStatus.NO_CONTENT);
        }
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());

        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        Floors floors = floorRepository.findByFloorIdAndParentId(id,user.getParentId());
        if (floors != null) {
            FloorsResponse floorsResponse = new FloorsMapper().apply(floors);
            return new ResponseEntity<>(floorsResponse, HttpStatus.OK);
        }

        return new ResponseEntity<>(Utils.INVALID, HttpStatus.NO_CONTENT);

    }

    public ResponseEntity<?> updateFloorById(int floorId, UpdateFloor updateFloor) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        Floors existingFloor = floorRepository.findByFloorIdAndParentId(floorId,user.getParentId());
        if (existingFloor == null) {
            return new ResponseEntity<>(Utils.INVALID, HttpStatus.NO_CONTENT);
        }

        if (updateFloor.floorName() != null && !updateFloor.floorName().isEmpty()) {
            int duplicateCount = floorRepository.countByFloorNameAndFloorId(
                    updateFloor.floorName(), existingFloor.getHostelId(), floorId
            );
            if (duplicateCount > 0) {
                return new ResponseEntity<>("Floor name already exists in this hostel", HttpStatus.CONFLICT);
            }
            existingFloor.setFloorName(updateFloor.floorName());
        }
        if (updateFloor.isActive() != null) {
            existingFloor.setIsActive(updateFloor.isActive());
        }
        existingFloor.setUpdatedAt(new Date());
        floorRepository.save(existingFloor);
        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);

    }

    public ResponseEntity<?> addFloor(AddFloors addFloors) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        HostelV1 hostelV1 = hostelV1Repository.findByHostelIdAndParentId(addFloors.hostelId(),user.getParentId());
        if (hostelV1==null){
            return new ResponseEntity<>("Hostel Doesn't found", HttpStatus.BAD_REQUEST);
        }
        int duplicateCount = floorRepository.countByFloorNameAndRoomAndHostelAndParent(
                addFloors.floorName(),
                addFloors.hostelId(),
                user.getParentId()
        );
        if (duplicateCount > 0) {
            return new ResponseEntity<>("Floor name already exists in this hostel", HttpStatus.CONFLICT);
        }

        Floors floors = new Floors();
        floors.setCreatedAt(new Date());
        floors.setUpdatedAt(new Date());
        floors.setParentId(user.getParentId());
        floors.setIsActive(true);
        floors.setIsDeleted(false);
        floors.setFloorName(addFloors.floorName());
        floors.setHostelId(addFloors.hostelId());
        floorRepository.save(floors);
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public ResponseEntity<?> deleteFloorById(int floorId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users users = usersService.findUserByUserId(userId);
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        Floors existingFloor = floorRepository.findByFloorIdAndParentId(floorId,users.getParentId());
        if (existingFloor != null) {
            floorRepository.delete(existingFloor);
            return new ResponseEntity<>("Deleted", HttpStatus.OK);
        }
        return new ResponseEntity<>("No Floor found", HttpStatus.BAD_REQUEST);

    }

    public boolean checkFloorExistForHostel(int floorId, String hostelId) {
        return floorRepository.findByFloorIdAndHostelId(floorId, hostelId) != null;
    }

}
