package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.Electricity.*;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.booking.BookedCustomer;
import com.smartstay.smartstay.dto.electricity.ElectricityCustomersList;
import com.smartstay.smartstay.dto.electricity.ElectricityHistoryBySingleCustomer;
import com.smartstay.smartstay.dto.electricity.ElectricityReadingForRoom;
import com.smartstay.smartstay.dto.electricity.ElectricityReadings;
import com.smartstay.smartstay.dto.room.RoomInfo;
import com.smartstay.smartstay.ennum.EBReadingType;
import com.smartstay.smartstay.ennum.ElectricityBillStatus;
import com.smartstay.smartstay.ennum.KycStatus;
import com.smartstay.smartstay.events.AddEbEvents;
import com.smartstay.smartstay.payloads.electricity.AddReading;
import com.smartstay.smartstay.repositories.ElectricityReadingRepository;
import com.smartstay.smartstay.responses.electricity.*;
import com.smartstay.smartstay.responses.rooms.RoomInfoForEB;
import com.smartstay.smartstay.rooms.EBReadingRoomsInfo;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

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
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private CustomerEbHistoryService ebHistoryService;
    @Autowired
    private CustomersService customerService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

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
        com.smartstay.smartstay.dao.ElectricityReadings electricityReadings = findLastEntryByRoomId(readings.roomId(), hostelId);

        Date billStartDate = new Date();
        Date billEndDate = new Date();

        if (electricityConfig != null && electricityConfig.getBillDate() != null) {
            Calendar cal = Calendar.getInstance();

            cal.setTime(date);
            cal.set(Calendar.DAY_OF_MONTH, electricityConfig.getBillDate());

            billStartDate = cal.getTime();
            billEndDate = Utils.findLastDate(electricityConfig.getBillDate(), date);

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

        com.smartstay.smartstay.dao.ElectricityReadings newReadings = new com.smartstay.smartstay.dao.ElectricityReadings();
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
        eventPublisher.publishEvent(new AddEbEvents(this, hostelId, readings.roomId(), readings.reading(), electricityConfig.getCharge(), date, users.getUserId(), electricityReadings));
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

    }

    public com.smartstay.smartstay.dao.ElectricityReadings findLastEntryByRoomId(Integer roomId, String hostelId) {
//        ElectricityReadings electricityReadings = electricityReadingRepository.findTopByRoomIdOrderByEntryDateDesc(roomId);
        return electricityReadingRepository.findTopByRoomIdAndHostelIdOrderByEntryDateDesc(roomId, hostelId);
    }

    private com.smartstay.smartstay.dao.ElectricityReadings findLatestEntryByHostelId(String hostelId) {
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

            String startDate = "";
            String endDate = "";
            com.smartstay.smartstay.dao.ElectricityReadings ebReading = electricityReadingRepository.findTopByHostelIdOrderByEntryDateDesc(hostelId);
            if (ebReading != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(ebReading.getBillEndDate());

                startDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + electricityConfig.getBillDate();
                endDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
            }
            List<ElectricityReadings> listElectricity = electricityReadingRepository.getElectricity(hostelId, startDate, endDate);
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

            com.smartstay.smartstay.dao.ElectricityReadings latestEntry = findLatestEntryByHostelId(hostelId);
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

    public ResponseEntity<?> getCustomersListElectricity(String hostelId) {
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
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_ELECTRIC_CITY, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        ElectricityConfig electricityConfig = hostelService.getElectricityConfig(hostelId);
        if (electricityConfig == null) {
            return new ResponseEntity<>(Utils.ELECTRICITY_CONFIG_NOT_SET_UP, HttpStatus.BAD_REQUEST);
        }


        List<CustomersList> listCustomers = new ArrayList<>();
        if (electricityConfig.getTypeOfReading().equalsIgnoreCase(EBReadingType.ROOM_READING.name())) {
            List<Integer> roomIds = electricityReadingRepository.getRoomIds(hostelId);
            List<ElectricityCustomersList> electricityCustomersLists = ebHistoryService.getCustomerListFromRooms(roomIds);
            listCustomers.addAll(electricityCustomersLists.stream()
                    .map(item -> {
                        return new CustomersListMapper().apply(item);
                    })
                    .toList());
        }
        else {
//            listCustomerForEBReadings

        }

        return new ResponseEntity<>(listCustomers, HttpStatus.OK);
    }

    public ResponseEntity<?> getRoomReadingsHistory(String hostelId, Integer roomId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_ELECTRIC_CITY, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!roomsService.checkRoomExistForHostel(roomId, hostelId)) {
            return new ResponseEntity<>(Utils.NO_ROOM_FOUND_HOSTEL, HttpStatus.BAD_REQUEST);
        }

        RoomInfo roomsDetails = roomsService.getRoom(roomId);
        List<ElectricityReadingForRoom> listElectricities = electricityReadingRepository.getRoomReading(roomId);

        EBReadingRoomsInfo roomInfo = null;

        if (roomsDetails != null) {
            roomInfo = new EBReadingRoomsInfo(roomsDetails.getRoomId(),
                    roomsDetails.getRoomName(),
                    roomsDetails.getFloorId(),
                    roomsDetails.getFloorName());
        }

        List<RoomElectricityList> roomList = new ArrayList<>();
        List<RoomElectricityCustomersList> listCustomers = new ArrayList<>();

        if (!listElectricities.isEmpty()) {
            for (int i=0; i<listElectricities.size(); i++) {
                if (i ==0 ) {
                    roomList.add(new ElectricityRoomMapper(null).apply(listElectricities.get(i)));
                }
                else {
                    roomList.add(new ElectricityRoomMapper(listElectricities.get(i-1)).apply(listElectricities.get(i)));
                }
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MONTH, 1);

            listCustomers = ebHistoryService.getCustomerEbListForRoom(roomId, calendar.getTime(), new Date());
        }



        RoomUsages roomUsages = new RoomUsages(roomInfo, roomList, listCustomers);

        return new ResponseEntity<>(roomUsages, HttpStatus.OK);
    }

    public ResponseEntity<?> getAllReadingByCustomer(String hostelId, String customerId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!Utils.checkNullOrEmpty(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }
        if (!Utils.checkNullOrEmpty(customerId)) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.BAD_REQUEST);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_ELECTRIC_CITY, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        Customers customers = customerService.getCustomerInformation(customerId);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }

        HostelV1 hostelV1 = hostelService.getHostelInfo(hostelId);
        int startDay = 1;
        if (hostelV1.getElectricityConfig() != null) {
            startDay = hostelV1.getElectricityConfig().getBillDate();
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, startDay);
        cal.set(Calendar.MONTH, 1);



        List<ElectricityHistoryBySingleCustomer> listEbHistory =  ebHistoryService.getAllReadingByCustomerId(customerId, cal.getTime(), new Date());

        StringBuilder fullName = new StringBuilder();
        StringBuilder initials = new StringBuilder();
        String bedName = listEbHistory.stream()
                .findFirst()
                .map(ElectricityHistoryBySingleCustomer::getBedName).orElse(null);
        String roomName = listEbHistory.stream()
                .findFirst()
                .map(ElectricityHistoryBySingleCustomer::getRoomName).orElse(null);;
        String floorName = listEbHistory.stream()
                .findFirst()
                .map(ElectricityHistoryBySingleCustomer::getFloorName).orElse(null);;
        String kycStatus = null;
        boolean isKycCompleted = false;

        if (customers.getFirstName() != null) {
            fullName.append(customers.getFirstName());
            initials.append(customers.getFirstName().toUpperCase().charAt(0));
        }
        if (customers.getLastName() != null && !customers.getLastName().trim().equalsIgnoreCase("")) {
            fullName.append(" ");
            fullName.append(customers.getLastName());
            initials.append(customers.getLastName().toUpperCase().charAt(0));
        }
        else {
            if (customers.getFirstName() != null && customers.getFirstName().length() > 1) {
                initials.append(customers.getFirstName().toUpperCase().charAt(1));
            }
        }

        if (customers.getKycStatus().equalsIgnoreCase(KycStatus.NOT_AVAILABLE.name())) {
            isKycCompleted = false;
            kycStatus = "NA";
        }
        else if (customers.getKycStatus().equalsIgnoreCase(KycStatus.PENDING.name())) {
            isKycCompleted = false;
            kycStatus = "Pending";
        }
        else if (customers.getKycStatus().equalsIgnoreCase(KycStatus.REQUESTED.name())) {
            isKycCompleted = false;
            kycStatus = "Requested";
        }
        else if (customers.getKycStatus().equalsIgnoreCase(KycStatus.VERIFIED.name())) {
            isKycCompleted = true;
            kycStatus = "Verified";
        }

        List<CustomersElectricityHistory> listHistory = listEbHistory
                .stream()
                .map(item -> new CustomerHistoryMapper()
                        .apply(item))
                .toList();

        ElectricitySingleCustomer singleCustomers = new ElectricitySingleCustomer(customers.getFirstName(),
                customers.getLastName(),
                initials.toString(),
                customers.getProfilePic(),
                customers.getCustomerId(),
                bedName,
                floorName,
                roomName,
                kycStatus,
                isKycCompleted,
                listHistory);

        return new ResponseEntity<>(singleCustomers, HttpStatus.OK);

    }
}
