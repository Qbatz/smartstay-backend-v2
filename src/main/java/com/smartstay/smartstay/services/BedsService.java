package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.BedsMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.ennum.BedStatus;
import com.smartstay.smartstay.payloads.beds.AddBed;
import com.smartstay.smartstay.payloads.beds.UpdateBed;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.responses.beds.BedsResponse;
import com.smartstay.smartstay.responses.beds.BedsStatusCount;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BedsService {

    @Autowired
    RolesRepository rolesRepository;

    @Autowired
    BedsRepository bedsRepository;

    @Autowired
    RoomRepository roomRepository;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;

    @Autowired
    private BookingsService bookingService;

    public ResponseEntity<?> getAllBeds(int roomId) {
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
        List<Beds> listBeds = bedsRepository.findAllByRoomIdAndParentId(roomId,users.getParentId());
        List<BedsResponse> bedsResponses = listBeds.stream().map(item -> new BedsMapper().apply(item)).toList();
        return new ResponseEntity<>(bedsResponses, HttpStatus.OK);
    }

    public ResponseEntity<?> getBedById(Integer id) {
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
        Beds bed = bedsRepository.findByBedIdAndParentId(id,user.getParentId());
        if (bed != null) {
            BedsResponse bedsResponse = new BedsMapper().apply(bed);
            return new ResponseEntity<>(bedsResponse, HttpStatus.OK);
        }else {
            return new ResponseEntity<>("Bed Doesn't exist", HttpStatus.BAD_REQUEST);
        }

    }

    public ResponseEntity<?> updateBedById(int bedId, UpdateBed updateBed) {
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
        Beds existingBed = bedsRepository.findByBedIdAndParentId(bedId,user.getParentId());
        if (existingBed == null) {
            return new ResponseEntity<>(Utils.INVALID, HttpStatus.NO_CONTENT);
        }

        if (updateBed.bedName() != null && !updateBed.bedName().isEmpty()) {
            int duplicateCount = bedsRepository.countByBedNameAndBedId(
                    user.getParentId(),bedId,existingBed.getRoomId()
            );
            if (duplicateCount > 0) {
                return new ResponseEntity<>("Bed name already exists in this room", HttpStatus.CONFLICT);
            }
            existingBed.setBedName(updateBed.bedName());
        }
        if (updateBed.isActive() != null) {
            existingBed.setIsActive(updateBed.isActive());
        }
        existingBed.setUpdatedAt(new Date());
        bedsRepository.save(existingBed);
        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);

    }

    public ResponseEntity<?> addBed(AddBed addBed) {
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
        boolean exists = roomRepository.checkRoomExistInTable(addBed.roomId(),user.getParentId(), addBed.hostelId()) == 1;
        if (!exists){
            return new ResponseEntity<>("Room Doesn't exist for this hostel", HttpStatus.BAD_REQUEST);
        }

        int duplicateCount = bedsRepository.countByBedNameAndRoomAndHostelAndParent(
                addBed.bedName(),
                addBed.roomId(),
                addBed.hostelId(),
                user.getParentId()
        );
        if (duplicateCount > 0) {
            return new ResponseEntity<>("Bed name already exists in this room", HttpStatus.CONFLICT);
        }

        Beds beds = new Beds();
        beds.setCreatedAt(new Date());
        beds.setUpdatedAt(new Date());
        beds.setIsActive(true);
        beds.setIsDeleted(false);
        beds.setBedName(addBed.bedName());
        beds.setParentId(user.getParentId());
        beds.setRoomId(addBed.roomId());
        beds.setHostelId(addBed.hostelId());
        beds.setRentAmount(addBed.amount());
        beds.setStatus(BedStatus.VACANT.name());
        beds.setFreeFrom(new Date());
        beds.setRentAmount(addBed.amount());
        bedsRepository.save(beds);
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public ResponseEntity<?> deleteBedById(int roomId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users users = usersService.findUserByUserId(userId);
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        Beds existingBed = bedsRepository.findByBedIdAndParentId(roomId,users.getParentId());
        if (existingBed != null) {
            bedsRepository.delete(existingBed);
            return new ResponseEntity<>("Deleted", HttpStatus.OK);
        }
        return new ResponseEntity<>("No Bed found", HttpStatus.BAD_REQUEST);

    }

    //assign bed
//    use it for checkin user
    public ResponseEntity<?> addUserToBed(int bedId, String joiningDate) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users users = usersService.findUserByUserId(userId);

        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_BOOKING, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Beds existingBed = bedsRepository.findByBedIdAndParentId(bedId,users.getParentId());
        if (existingBed != null) {
            if (Utils.compareWithTodayDate(Utils.stringToDate(joiningDate))) {
                existingBed.setStatus(BedStatus.OCCUPIED.name());
                existingBed.setBooked(true);
                existingBed.setUpdatedAt(new Date());
                existingBed.setFreeFrom(null);
            }

            bedsRepository.save(existingBed);

        }
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public boolean isBedAvailable(int bedId, String parentId, Date date) {
        Beds beds = bedsRepository.findByBedIdAndParentId(bedId, parentId);
        if (beds.getStatus().equalsIgnoreCase(BedStatus.VACANT.name())) {
            return true;
        }
        else if (beds.getStatus().equalsIgnoreCase(BedStatus.OCCUPIED.name())) {
            return false;
        }
        else if (beds.getStatus().equalsIgnoreCase(BedStatus.NOTICE.name())) {
            BookingsV1 bookingsV1 = bookingService.checkLatestStatusForBed(bedId);
            if (bookingsV1.getLeavingDate() != null) {
                if (Utils.compareWithTwoDates(bookingsV1.getLeavingDate(), date) > 0) {
                    return false;
                }
                else  {
                    return true;
                }
            }

        }
        else if (beds.getStatus().equalsIgnoreCase(BedStatus.BOOKED.name())) {
            BookingsV1 bookingsV1 = bookingService.checkLatestStatusForBed(bedId);

            if (bookingsV1.getLeavingDate() != null) {
                if (Utils.compareWithTwoDates(bookingsV1.getJoiningDate(), date) > 0) {
                    return true;
                }
                else  {
                    return false;
                }
            }
        }

        return true;

    }

    public boolean checkBedExistForRoom(int bedId, int roomId, String hostelId) {
        return bedsRepository.findByBedIdAndRoomIdAndHostelId(bedId, roomId, hostelId) != null;
    }

    public List<BedsStatusCount> findBedCount(String hostelId) {

        return bedsRepository.getBedCountByStatus(hostelId);
    }
}
