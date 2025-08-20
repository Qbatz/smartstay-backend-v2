package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.RoomsMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.payloads.rooms.AddRoom;
import com.smartstay.smartstay.payloads.rooms.UpdateRoom;
import com.smartstay.smartstay.repositories.FloorRepository;
import com.smartstay.smartstay.repositories.HostelV1Repository;
import com.smartstay.smartstay.repositories.RolesRepository;
import com.smartstay.smartstay.repositories.RoomRepository;
import com.smartstay.smartstay.responses.rooms.RoomsResponse;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class RoomsService {

    @Autowired
    RolesRepository rolesRepository;

    @Autowired
    HostelV1Repository hostelV1Repository;

    @Autowired
    FloorRepository floorRepository;

    @Autowired
    RoomRepository roomRepository;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;

    public ResponseEntity<?> getAllRooms(int floorId) {
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
        List<Rooms> listRooms = roomRepository.findAllByFloorIdAndParentId(floorId,users.getParentId());
        List<RoomsResponse> roomsResponses = listRooms.stream().map(item -> new RoomsMapper().apply(item)).toList();
        return new ResponseEntity<>(roomsResponses, HttpStatus.OK);
    }

    public ResponseEntity<?> getRoomById(Integer id) {
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
        Rooms rooms = roomRepository.findByRoomIdAndParentId(id,user.getParentId());
        if (rooms != null) {
            RoomsResponse roomsResponse = new RoomsMapper().apply(rooms);
            return new ResponseEntity<>(roomsResponse, HttpStatus.OK);
        }

        return new ResponseEntity<>(Utils.INVALID, HttpStatus.NO_CONTENT);

    }

    public ResponseEntity<?> updateRoomById(String hostelId,int roomId, UpdateRoom updateRoom) {
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
        Rooms existingRoom = roomRepository.findByRoomIdAndParentIdAndHostelId(roomId,user.getParentId(),hostelId);
        if (existingRoom == null) {
            return new ResponseEntity<>(Utils.INVALID, HttpStatus.NO_CONTENT);
        }
        if (updateRoom.roomName() != null && !updateRoom.roomName().isEmpty()) {
            existingRoom.setRoomName(updateRoom.roomName());
        }
        if (updateRoom.isActive() != null) {
            existingRoom.setIsActive(updateRoom.isActive());
        }
        existingRoom.setUpdatedAt(new Date());
        roomRepository.save(existingRoom);
        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);

    }

    public ResponseEntity<?> addRoom(AddRoom addRoom) {
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
        Floors floors = floorRepository.findByFloorIdAndParentId(addRoom.floorId(),user.getParentId());
        if (floors==null){
            return new ResponseEntity<>("Floor Doesn't exist", HttpStatus.BAD_REQUEST);
        }

        Floors floors1 = floorRepository.findByFloorIdAndParentIdAndHostelId(addRoom.floorId(),user.getParentId(), addRoom.hostelId());
        if (floors1==null){
            return new ResponseEntity<>("Floor Doesn't exist for this hostel", HttpStatus.BAD_REQUEST);
        }

        Rooms room = new Rooms();
        room.setCreatedAt(new Date());
        room.setUpdatedAt(new Date());
        room.setIsActive(true);
        room.setIsDeleted(false);
        room.setRoomName(addRoom.roomName());
        room.setParentId(user.getParentId());
        room.setFloorId(addRoom.floorId());
        room.setHostelId(addRoom.hostelId());
        roomRepository.save(room);
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public ResponseEntity<?> deleteRoomById(int roomId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users users = usersService.findUserByUserId(userId);
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        Rooms existingRoom = roomRepository.findByRoomIdAndParentId(roomId,users.getParentId());
        if (existingRoom != null) {
            roomRepository.delete(existingRoom);
            return new ResponseEntity<>("Deleted", HttpStatus.OK);
        }
        return new ResponseEntity<>("No Room found", HttpStatus.BAD_REQUEST);

    }

    public boolean checkRoomExistForFloor(int floorId, int roomId) {
        return roomRepository.findByRoomIdAndFloorId(roomId, floorId) != null;
    }
}
