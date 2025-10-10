package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.Electricity.*;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.ElectricityConfig;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.booking.BookedCustomer;
import com.smartstay.smartstay.dto.electricity.ElectricityReadingForRoom;
import com.smartstay.smartstay.dto.electricity.ElectricityReadings;
import com.smartstay.smartstay.dto.room.RoomInfo;
import com.smartstay.smartstay.ennum.EBReadingType;
import com.smartstay.smartstay.ennum.ElectricityBillStatus;
import com.smartstay.smartstay.payloads.electricity.AddReading;
import com.smartstay.smartstay.repositories.ElectricityReadingRepository;
import com.smartstay.smartstay.responses.electricity.*;
import com.smartstay.smartstay.responses.rooms.RoomInfoForEB;
import com.smartstay.smartstay.rooms.EBReadingRoomsInfo;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
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
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

    }

    private com.smartstay.smartstay.dao.ElectricityReadings findLastEntryByRoomId(Integer roomId, String hostelId) {
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

        if (electricityConfig.getTypeOfReading().equalsIgnoreCase(EBReadingType.ROOM_READING.name())) {
            String startDate = "";
            String endDate = "";
            com.smartstay.smartstay.dao.ElectricityReadings ebReading = electricityReadingRepository.findTopByHostelIdOrderByEntryDateDesc(hostelId);
            Calendar calendar = Calendar.getInstance();
            if (ebReading != null) {
                calendar.setTime(ebReading.getBillEndDate());

                startDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + electricityConfig.getBillDate();
                if (Utils.compareWithTwoDates(new Date(), ebReading.getBillEndDate()) < 0) {
                    endDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
                }
                else {
                    endDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
                }

            }
            List<ElectricityReadings> listElectricity = electricityReadingRepository.getElectricityForCustomers(hostelId, startDate, endDate);

            Date ebEndDate = Utils.stringToDate( calendar.get(Calendar.DAY_OF_MONTH) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.YEAR), Utils.USER_INPUT_DATE_FORMAT);
            Date ebStartDate = Utils.stringToDate(electricityConfig.getBillDate()+ "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.YEAR), Utils.USER_INPUT_DATE_FORMAT);
            List<Integer> roomIds = listElectricity
                    .stream()
                    .map(ElectricityReadings::getRoomId)
                    .toList();

            List<BookedCustomer> listCustomers =  bookingsService.findBookedCustomers(roomIds, ebStartDate, ebEndDate);

            List<CustomersList> listCustomerForEBReadings = new ArrayList<>();
            roomIds.forEach(item -> {
                List<BookedCustomer>  list = listCustomers.
                        stream().
                        filter(cus -> Objects.equals(item, cus.getRoomId())).toList();

                if (!list.isEmpty()) {
                    AtomicInteger count = new AtomicInteger(0);
                    list.forEach(filtered -> {
                        System.out.println(filtered.toString());
                        //joined before the eb cycle
                        if (Utils.compareWithTwoDates(filtered.getJoiningDate(), ebStartDate) <= 0) {
                            count.set(count.get() + 1);
                        }
                    });

                    ElectricityReadings readings = listElectricity
                            .stream()
                            .filter(i -> item == i.getRoomId())
                            .findFirst().orElse(null);

                    //this executed, when all the customers joined before eb start date
                    if (count.get() == list.size()) {
                        double totalAmount = 0;
                        double finalUnits;
                        if (readings != null && readings.getConsumption() != null) {
                            finalUnits = readings.getConsumption() / count.get();
                            totalAmount = readings.getConsumption() * readings.getUnitPrice();
                        } else {
                            finalUnits = 0;
                        }
                        double finalPrice = totalAmount/count.get();
                        System.out.println(finalPrice);

                        listCustomerForEBReadings.addAll(list.stream()
                                .map(i -> new CustomersListMapper(finalPrice, finalUnits, readings.getStartDate(), readings.getEntryDate()).apply(i))
                                .toList());
                    }
                    else {
                        AtomicInteger noOfpersons = new AtomicInteger();
                        list.forEach(notOneToEnd -> {
                            if (readings != null) {
                                if (notOneToEnd.getLeavingDate() != null && Utils.compareWithTwoDates(notOneToEnd.getLeavingDate(), readings.getEntryDate()) <=0) {
                                    if (notOneToEnd.getJoiningDate() != null && Utils.compareWithTwoDates(notOneToEnd.getJoiningDate(), readings.getStartDate()) > 0) {
                                        noOfpersons.set(noOfpersons.get() + (int) Utils.findNumberOfDays(notOneToEnd.getJoiningDate(), notOneToEnd.getLeavingDate()));
                                    }
                                    else {
                                        noOfpersons.set(noOfpersons.get() + (int) Utils.findNumberOfDays(readings.getStartDate(), notOneToEnd.getLeavingDate()));
                                    }
                                }
                                else if (notOneToEnd.getLeavingDate() == null && notOneToEnd.getJoiningDate() != null) {
                                    if (Utils.compareWithTwoDates(notOneToEnd.getJoiningDate(), readings.getStartDate()) > 0) {
                                        noOfpersons.set(noOfpersons.get() + (int) Utils.findNumberOfDays(notOneToEnd.getJoiningDate(), readings.getEndDate()));
                                    }
                                    else if (Utils.compareWithTwoDates(notOneToEnd.getJoiningDate(), readings.getStartDate()) <= 0) {
                                        noOfpersons.set(noOfpersons.get() + (int) Utils.findNumberOfDays(readings.getStartDate(), readings.getEndDate()));
                                    }


                                }

                            }

                        });
                        double ebPrice = 0;
                        double finalPricePerPerson; //for individual day
                        double finalUsagePerPerson;
                        if (readings != null) {
                            ebPrice = readings.getConsumption() * readings.getUnitPrice();
                            finalPricePerPerson = ebPrice / noOfpersons.get();
                            finalUsagePerPerson = readings.getConsumption() / noOfpersons.get();
                        } else {
                            finalUsagePerPerson = 0;
                            finalPricePerPerson = 0;
                        }


                        list.forEach(eachPerson -> {
                            if (readings != null) {
                                if (eachPerson.getLeavingDate() == null) {
                                    if (Utils.compareWithTwoDates(eachPerson.getJoiningDate(), readings.getStartDate()) <= 0) {
                                        int noOfDays = (int) Utils.findNumberOfDays(readings.getStartDate(), readings.getEntryDate());
                                        double finalPrice = finalPricePerPerson * noOfDays;
                                        double finalUsage = finalUsagePerPerson * noOfDays;
                                        listCustomerForEBReadings.add(new CustomersListMapper(finalPrice, finalUsage, readings.getStartDate(), readings.getEntryDate()).apply(eachPerson));
                                    }
                                    else if (Utils.compareWithTwoDates(eachPerson.getJoiningDate(), readings.getStartDate()) > 0) {
                                        int noOfDays = (int) Utils.findNumberOfDays(eachPerson.getJoiningDate(), readings.getEntryDate());
                                        double finalPrice = finalPricePerPerson * noOfDays;
                                        double finalUsage = finalUsagePerPerson * noOfDays;
                                        listCustomerForEBReadings.add(new CustomersListMapper(finalPrice, finalUsage, eachPerson.getJoiningDate(), readings.getEntryDate()).apply(eachPerson));
                                    }
                                }
                                else if (eachPerson.getLeavingDate() != null) {
                                    if (Utils.compareWithTwoDates(eachPerson.getLeavingDate(), readings.getEntryDate()) <= 0) {
                                        if (Utils.compareWithTwoDates(eachPerson.getJoiningDate(), readings.getStartDate()) <= 0) {
                                            int noOfDays = (int) Utils.findNumberOfDays(readings.getStartDate(), eachPerson.getLeavingDate());
                                            double finalPrice = finalPricePerPerson * noOfDays;
                                            double finalUsage = finalUsagePerPerson * noOfDays;
                                            listCustomerForEBReadings.add(new CustomersListMapper(finalPrice, finalUsage, readings.getStartDate(), eachPerson.getLeavingDate()).apply(eachPerson));
                                        }
                                        else {
                                            int noOfDays = (int) Utils.findNumberOfDays(eachPerson.getJoiningDate(), eachPerson.getLeavingDate());
                                            double finalPrice = finalPricePerPerson * noOfDays;
                                            double finalUsage = finalUsagePerPerson * noOfDays;
                                            listCustomerForEBReadings.add(new CustomersListMapper(finalPrice, finalUsage, eachPerson.getJoiningDate(), eachPerson.getLeavingDate()).apply(eachPerson));
                                        }
                                    }
                                    else {
                                        if (Utils.compareWithTwoDates(eachPerson.getJoiningDate(), readings.getStartDate()) <= 0) {
                                            int noOfDays = (int) Utils.findNumberOfDays(readings.getStartDate(), readings.getEndDate());
                                            double finalPrice = finalPricePerPerson * noOfDays;
                                            double finalUsage = finalUsagePerPerson * noOfDays;
                                            listCustomerForEBReadings.add(new CustomersListMapper(finalPrice, finalUsage, readings.getStartDate(), readings.getEntryDate()).apply(eachPerson));
                                        }
                                        else {
                                            int noOfDays = (int) Utils.findNumberOfDays(eachPerson.getJoiningDate(), readings.getEndDate());
                                            double finalPrice = finalPricePerPerson * noOfDays;
                                            double finalUsage = finalUsagePerPerson * noOfDays;
                                            listCustomerForEBReadings.add(new CustomersListMapper(finalPrice, finalUsage, eachPerson.getJoiningDate(), readings.getEntryDate()).apply(eachPerson));
                                        }
                                    }

                                }
                            }

                        });
                    }
                }
            });

            return new ResponseEntity<>(listCustomerForEBReadings, HttpStatus.OK);

        }


        return null;
    }

    public ResponseEntity<?> getRoomReadingsHistory(String hostelId, String roomId) {
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
        if (!roomsService.checkRoomExistForHostel(Integer.parseInt(roomId), hostelId)) {
            return new ResponseEntity<>(Utils.NO_ROOM_FOUND_HOSTEL, HttpStatus.BAD_REQUEST);
        }

        RoomInfo roomsDetails = roomsService.getRoom(Integer.parseInt(roomId));
        List<ElectricityReadingForRoom> listElectricities = electricityReadingRepository.getRoomReading(roomId);

        EBReadingRoomsInfo roomInfo = null;

        if (roomsDetails != null) {
            roomInfo = new EBReadingRoomsInfo(roomsDetails.getRoomId(),
                    roomsDetails.getRoomName(),
                    roomsDetails.getFloorId(),
                    roomsDetails.getFloorName());
        }

        List<RoomElectricityList> roomList = new ArrayList<>();

        if (!listElectricities.isEmpty()) {
            for (int i=0; i<listElectricities.size(); i++) {
                if (i ==0 ) {
                    roomList.add(new ElectricityRoomMapper(null).apply(listElectricities.get(i)));
                }
                else {
                    roomList.add(new ElectricityRoomMapper(listElectricities.get(i-1)).apply(listElectricities.get(i)));
                }
            }
        }


        RoomUsages roomUsages = new RoomUsages(roomInfo, roomList);

        return new ResponseEntity<>(roomUsages, HttpStatus.OK);
    }
}
