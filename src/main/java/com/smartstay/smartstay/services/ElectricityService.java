package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.Electricity.ListReadingMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.ElectricityConfig;
import com.smartstay.smartstay.dao.ElectricityReadings;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.electricity.ElectricityReaddings;
import com.smartstay.smartstay.ennum.EBReadingType;
import com.smartstay.smartstay.ennum.ElectricityBillStatus;
import com.smartstay.smartstay.payloads.electricity.AddReading;
import com.smartstay.smartstay.repositories.ElectricityReadingRepository;
import com.smartstay.smartstay.responses.electricity.ElectricityUsage;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ElectricityService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private ElectricityReadingRepository electricityReadingRepository;

    public ResponseEntity<?> addMeterReading(String hostelId, AddReading readings) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!Utils.checkNullOrEmpty(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }

        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_ELECTRIC_CITY, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        ElectricityConfig electricityConfig = hostelService.getElectricityConfig(hostelId);
        if (electricityConfig == null) {
            return new ResponseEntity<>(Utils.ELECTRICITY_CONFIG_NOT_SET_UP, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Date date = null;
        Double previousReading = 0.0;
        if (Utils.checkNullOrEmpty(readings.readingDate())) {
            date = new Date();
        }
        else {
            date = Utils.stringToDate(readings.readingDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        }
        ElectricityReadings electricityReadings = findLastEntryByRoomId(readings.roomId());

        if (electricityConfig.getTypeOfReading().equalsIgnoreCase(EBReadingType.ROOM_READING.name())) {
            if (!Utils.checkNullOrEmpty(readings.roomId())) {
                return new ResponseEntity<>(Utils.INVALID_ROOM_ID, HttpStatus.BAD_REQUEST);
            }

            if (electricityReadings == null) {
                previousReading = 0.0;
            }
            else {
                previousReading = electricityReadings.getCurrentReading();
            }
        }
        else {
            if (electricityReadings == null) {
                previousReading = 0.0;
            }
            else {
                previousReading = electricityReadings.getCurrentReading();
            }
        }

        if (electricityReadings != null) {
            if (Utils.compareWithTwoDates(electricityReadings.getEntryDate(), Utils.stringToDate(readings.readingDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT)) >=0) {
                return new ResponseEntity<>(Utils.ALREADY_READING_TAKEN_THIS_DATE, HttpStatus.BAD_REQUEST);
            }
        }


        if (readings.reading() < previousReading) {
            return new ResponseEntity<>(Utils.PREVIOUD_CURRENT_READING_NOT_MATCHING, HttpStatus.BAD_REQUEST);
        }

        ElectricityReadings newReadings = new ElectricityReadings();
        newReadings.setPreviousReading(previousReading);
        newReadings.setCurrentReading(readings.reading());
        newReadings.setHostelId(hostelId);
        newReadings.setRoomId(readings.roomId());
        newReadings.setCurrentUnitPrice(electricityConfig.getCharge());
        newReadings.setEntryDate(date);
        newReadings.setBillStatus(ElectricityBillStatus.INVOICE_NOT_GENERATED.name());
        newReadings.setFloorId(readings.floorId());
        newReadings.setConsumption(readings.reading() - previousReading);
        newReadings.setMissedEntry(false);
        newReadings.setCreatedAt(new Date());
        newReadings.setUpdatedAt(new Date());
        newReadings.setCreatedBy(users.getUserId());
        newReadings.setUpdatedBy(users.getUserId());

        electricityReadingRepository.save(newReadings);
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

    }

    private ElectricityReadings findLastEntryByRoomId(Integer roomId) {
//        ElectricityReadings electricityReadings = electricityReadingRepository.findTopByRoomIdOrderByEntryDateDesc(roomId);
        return electricityReadingRepository.findTopByRoomIdOrderByEntryDateDesc(roomId);
    }

    private ElectricityReadings findLatestEntryByHostelId(String hostelId) {
        return electricityReadingRepository.findTopByHostelIdOrderByEntryDateDesc(hostelId);
    }

    public ResponseEntity<?> getEBReadings(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!Utils.checkNullOrEmpty(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(),hostelId)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_ELECTRIC_CITY, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        List<ElectricityReaddings> listElectricities = electricityReadingRepository.getElectricity(hostelId);

        List<ElectricityUsage> listUsages = listElectricities
                .stream()
                .map(item -> new ListReadingMapper().apply(item))
                .toList();

        return new ResponseEntity<>(listUsages, HttpStatus.OK);
    }
}
