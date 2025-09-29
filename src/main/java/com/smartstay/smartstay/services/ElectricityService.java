package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.Electricity.ElectricityHostelBasedMapper;
import com.smartstay.smartstay.Wrappers.Electricity.ElectricityUsageMapper;
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
import com.smartstay.smartstay.responses.electricity.ElectricityList;
import com.smartstay.smartstay.responses.electricity.ElectricityUsage;
import com.smartstay.smartstay.responses.rooms.RoomInfoForEB;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    private RoomsService roomsService;

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
        if (!Utils.checkNullOrEmpty(readings.readingDate())) {
            date = new Date();
        }
        else {
            date = Utils.stringToDate(readings.readingDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        }
        ElectricityReadings electricityReadings = findLastEntryByRoomId(readings.roomId(), hostelId);

        Date billStartDate = new Date();
        Date billEndDate = new Date();

        if (electricityConfig != null && electricityConfig.getBillDate() != null) {
            Calendar cal = Calendar.getInstance();

            cal.setTime(date);
            cal.set(Calendar.DAY_OF_MONTH, electricityConfig.getBillDate());

            billStartDate = cal.getTime();
            billEndDate = Utils.findLastDate(electricityConfig.getBillDate());

        }


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
            if (Utils.compareWithTwoDates(Utils.stringToDate(readings.readingDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT), electricityReadings.getEntryDate()) <=0) {
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
        newReadings.setBillStartDate(billStartDate);
        newReadings.setBillEndDate(billEndDate);

        electricityReadingRepository.save(newReadings);
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

    }

    private ElectricityReadings findLastEntryByRoomId(Integer roomId, String hostelId) {
//        ElectricityReadings electricityReadings = electricityReadingRepository.findTopByRoomIdOrderByEntryDateDesc(roomId);
        return electricityReadingRepository.findTopByRoomIdAndHostelIdOrderByEntryDateDesc(roomId, hostelId);
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

        ElectricityConfig electricityConfig = hostelService.getElectricityConfig(hostelId);
        //this is for room reading
        if (electricityConfig.getTypeOfReading().equalsIgnoreCase(EBReadingType.ROOM_READING.name())) {
            List<ElectricityReaddings> listElectricity = electricityReadingRepository.getElectricity(hostelId);
            List<Integer> listRoomsInMeterReadings = new ArrayList<>();


            List<ElectricityUsage> listUsages = new ArrayList<>(listElectricity
                    .stream()
                    .map(item -> {
                        listRoomsInMeterReadings.add(item.getRoomId());
                        return new ListReadingMapper().apply(item);
                    })
                    .toList());

            List<RoomInfoForEB> listBedForEb = roomsService.getBedsNotRegisteredOnEB(listRoomsInMeterReadings, hostelId);
            if (!listBedForEb.isEmpty()) {
                List<ElectricityUsage> usages =  listBedForEb.stream()
                        .map(item -> new ElectricityUsageMapper().apply(item))
                        .toList();

                listUsages.addAll(usages);

            }


            ElectricityList list = new ElectricityList(false, listUsages);

            return new ResponseEntity<>(list, HttpStatus.OK);
        }
        else {

            ElectricityReadings latestEntry = findLatestEntryByHostelId(hostelId);
            List<RoomInfoForEB> listRoomInfo =  roomsService.findAllRoomsByHostelId(hostelId);
            ElectricityList list = null;
                if (listRoomInfo != null) {
                    List<ElectricityUsage> listUsage = listRoomInfo.stream()
                            .map(item -> new ElectricityHostelBasedMapper(listRoomInfo.size(),
                                    hostelId,
                                    latestEntry).apply(item))
                            .toList();

                    list = new ElectricityList(true, listUsage);
                    return new ResponseEntity<>(list, HttpStatus.OK);
                }
            list = new ElectricityList(true, null);
            return new ResponseEntity<>(list, HttpStatus.OK);
        }


    }
}
