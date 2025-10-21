package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.Electricity.*;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dao.ElectricityReadings;
import com.smartstay.smartstay.dto.booking.BookedCustomer;
import com.smartstay.smartstay.dto.electricity.*;
import com.smartstay.smartstay.dto.room.RoomInfo;
import com.smartstay.smartstay.ennum.EBReadingType;
import com.smartstay.smartstay.ennum.ElectricityBillStatus;
import com.smartstay.smartstay.ennum.KycStatus;
import com.smartstay.smartstay.events.AddEbEvents;
import com.smartstay.smartstay.payloads.electricity.AddReading;
import com.smartstay.smartstay.repositories.ElectricityReadingRepository;
import com.smartstay.smartstay.responses.electricity.*;
import com.smartstay.smartstay.responses.electricity.RoomElectricityCustomersList;
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
        Date date = null;
        if (readings.readingDate() != null) {
            date = Utils.stringToDate(readings.readingDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            if (Utils.compareWithTwoDates(new Date(), date) < 0) {
                return new ResponseEntity<>(Utils.FUTURE_DATES_NOT_ALLOWED, HttpStatus.BAD_REQUEST);
            }
        }

        ElectricityConfig electricityConfig = hostelService.getElectricityConfig(hostelId);
        if (electricityConfig == null) {
            return new ResponseEntity<>(Utils.ELECTRICITY_CONFIG_NOT_SET_UP, HttpStatus.UNPROCESSABLE_ENTITY);
        }


        Double previousReading = 0.0;
        if (date == null) {
            date = new Date();
        }


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

            com.smartstay.smartstay.dao.ElectricityReadings electricityReadings = findLastEntryByRoomId(readings.roomId(), hostelId);

            if (electricityReadings != null) {
                if (Utils.compareWithTwoDates(Utils.stringToDate(readings.readingDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT), electricityReadings.getEntryDate()) <=0) {
                    return new ResponseEntity<>(Utils.ALREADY_READING_TAKEN_THIS_DATE, HttpStatus.BAD_REQUEST);
                }
            }

            if (electricityReadings == null) {
                previousReading = 0.0;
            }
            else {
                previousReading = electricityReadings.getCurrentReading();
            }

            if (readings.reading() < previousReading) {
                return new ResponseEntity<>(Utils.PREVIOUS_CURRENT_READING_NOT_MATCHING, HttpStatus.BAD_REQUEST);
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

            com.smartstay.smartstay.dao.ElectricityReadings newReading = electricityReadingRepository.save(newReadings);
            eventPublisher.publishEvent(new AddEbEvents(this, hostelId, readings.roomId(), readings.reading(), electricityConfig.getCharge(), date, users.getUserId(), electricityReadings, newReading.getId()));
            return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
        }
        else {
            return addMeterReadingForHostelBased(hostelId, readings, electricityConfig);
        }


    }

    public ResponseEntity<?> addMeterReadingForHostelBased(String hostelId, AddReading readings, ElectricityConfig electricityConfig) {

        Date date = null;
        Date billStartDate = new Date();
        Date billEndDate = new Date();
        boolean isProRate = true;
        double totalBillAmount = 0.0;

        CurrentReadings currentReadings = electricityReadingRepository.getCurrentReadings(hostelId);
        List<ElectricityRoomIdFromPreviousEntry> previousEntries = new ArrayList<>();

        if (readings.readingDate() != null) {
            date = Utils.stringToDate(readings.readingDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            if (Utils.compareWithTwoDates(new Date(), date) < 0) {
                return new ResponseEntity<>(Utils.FUTURE_DATES_NOT_ALLOWED, HttpStatus.BAD_REQUEST);
            }
            if (currentReadings != null) {
                if (Utils.compareWithTwoDates(Utils.stringToDate(readings.readingDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT), currentReadings.getEntryDate()) <=0) {
                    return new ResponseEntity<>(Utils.ALREADY_READING_TAKEN_THIS_DATE, HttpStatus.BAD_REQUEST);
                }

                billStartDate = currentReadings.getEntryDate();

            }

            if (electricityConfig != null && electricityConfig.getBillDate() != null) {

                if (currentReadings == null) {
                    Calendar startDate = Calendar.getInstance();

                    startDate.setTime(date);
                    startDate.set(Calendar.DAY_OF_MONTH, startDate.get(Calendar.DAY_OF_MONTH) -1);
                    startDate.set(Calendar.DAY_OF_MONTH, electricityConfig.getBillDate());

                    billStartDate = startDate.getTime();
                }

                billEndDate = date;
                isProRate = electricityConfig.isProRate();
            }

        }

        Double previousReading = 0.0;
        if (date == null) {
            date = new Date();
        }

//        com.smartstay.smartstay.dao.ElectricityReadings electricityReadings = findLatestEntryByHostelId(hostelId);



        if (currentReadings == null) {
            previousReading = 0.0;
        }
        else {
            previousReading = currentReadings.getCurrentReading();
            previousEntries = electricityReadingRepository.getRoomIdsFromPreviousEntry(hostelId, currentReadings.getEntryDate());
        }

        if (readings.reading() <= previousReading) {
            return new ResponseEntity<>(Utils.PREVIOUS_CURRENT_READING_NOT_MATCHING, HttpStatus.BAD_REQUEST);
        }

        List<CustomerBedsList> listBeds =  customerService.getCustomersFromBedHistory(hostelId, billStartDate, billEndDate);
        double currentConsumption = readings.reading() - previousReading;

        if (!isProRate) {
            double unitPerPerson = currentConsumption/listBeds.size();

            HashMap<Integer, Integer> personCountPerRoom = new HashMap<>();
            listBeds.forEach(item -> {
                if (personCountPerRoom.containsKey(item.roomId())) {
                    personCountPerRoom.put(item.roomId(), personCountPerRoom.get(item.roomId()) + 1);
                }
                else {
                    personCountPerRoom.put(item.roomId(), 1);
                }
            });

            List<ElectricityReadings> listNewElectricityReading = new ArrayList<>();
            for (Integer key: personCountPerRoom.keySet()) {
                com.smartstay.smartstay.dao.ElectricityReadings newReadings = new com.smartstay.smartstay.dao.ElectricityReadings();
                com.smartstay.smartstay.dao.ElectricityReadings roomPreviousReadings = electricityReadingRepository.getRoomCurrentReading(key);
                if (roomPreviousReadings == null) {
                    newReadings.setPreviousReading(0.0);
                    newReadings.setCurrentReading((unitPerPerson * personCountPerRoom.get(key)));
                }
                else {
                    newReadings.setPreviousReading(roomPreviousReadings.getCurrentReading());
                    newReadings.setCurrentReading((unitPerPerson * personCountPerRoom.get(key)) + roomPreviousReadings.getCurrentReading());
                }
                newReadings.setHostelId(hostelId);
                newReadings.setRoomId(key);
                newReadings.setCurrentUnitPrice(electricityConfig.getCharge());
                newReadings.setEntryDate(date);
                newReadings.setBillStatus(ElectricityBillStatus.INVOICE_NOT_GENERATED.name());
                newReadings.setFloorId(readings.floorId());
                newReadings.setConsumption((unitPerPerson * personCountPerRoom.get(key)));
                newReadings.setMissedEntry(false);
                newReadings.setCreatedAt(new Date());
                newReadings.setUpdatedAt(new Date());
                newReadings.setCreatedBy(authentication.getName());
                newReadings.setUpdatedBy(authentication.getName());
                newReadings.setBillStartDate(billStartDate);
                newReadings.setBillEndDate(billEndDate);

                listNewElectricityReading.add(newReadings);

            }

            List<ElectricityReadings> listReadingsAfterSaving = electricityReadingRepository.saveAll(listNewElectricityReading);
            Date finalBillEndDate = billEndDate;
            Date finalBillStartDate = billStartDate;
            listReadingsAfterSaving.forEach(item -> {


                        List<CustomerBedsList> listCusId = listBeds
                                .stream()
                                .filter(i -> Objects.equals(i.roomId(), item.getRoomId()))
                                .toList();

                        List<CustomersEbHistory> customerEbHistory = listCusId
                                .stream()
                                .map(i -> {
                                    CustomersEbHistory ebHistory = new CustomersEbHistory();
                                    ebHistory.setReadingId(item.getId());
                                    ebHistory.setCustomerId(i.customerId());
                                    ebHistory.setRoomId(item.getRoomId());
                                    ebHistory.setFloorId(item.getFloorId());
                                    ebHistory.setBedId(i.bedId());
                                    ebHistory.setUnits(unitPerPerson);
                                    ebHistory.setAmount(unitPerPerson * electricityConfig.getCharge());
                                    ebHistory.setStartDate(finalBillStartDate);
                                    if (i.endDate() == null) {
                                        ebHistory.setEndDate(finalBillEndDate);
                                    }
                                    else {
                                        if (Utils.compareWithTwoDates(finalBillEndDate, i.endDate()) <= 0) {
                                            ebHistory.setEndDate(finalBillEndDate);
                                        }
                                        else {
                                            ebHistory.setEndDate(i.endDate());
                                        }
                                    }

                                    ebHistory.setCreatedAt(new Date());
                                    ebHistory.setCreatedBy(ebHistory.getCreatedBy());

                                    return ebHistory;
                                })
                                .toList();

                        ebHistoryService.saveCustomerEb(customerEbHistory);
                    });

        }
        else {
            Date readingDate = null;
            Date startDate = null;
            Date endDate = null;

            if (readings.readingDate() != null) {
                readingDate = Utils.stringToDate(readings.readingDate(), Utils.USER_INPUT_DATE_FORMAT);
                endDate = readingDate;
            }
            else {
                readingDate = new Date();
            }
            if (currentReadings != null) {

                Calendar cal = Calendar.getInstance();
                cal.setTime(currentReadings.getEntryDate());
                cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);

                startDate = cal.getTime();
            }
            else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(readingDate);
                assert electricityConfig != null;
                cal.set(Calendar.DAY_OF_MONTH, electricityConfig.getBillDate());

                startDate = cal.getTime();


                Calendar calEndDate = Calendar.getInstance();
                calEndDate.setTime(readingDate);

                endDate = calEndDate.getTime();

            }
            Date finalStartDate = startDate;
            Date finalReadingDate = endDate;

            int totalDays = listBeds.stream()
                    .mapToInt(item -> {
                        assert currentReadings != null;
                        if (Utils.compareWithTwoDates(item.startDate(), finalStartDate) <= 0) {
                            if (item.endDate() == null) {
                                return Math.toIntExact(Utils.findNumberOfDays(finalStartDate, finalReadingDate));
                            }
                            else if (Utils.compareWithTwoDates(item.endDate(), finalReadingDate) <= 0) {
                                return Math.toIntExact(Utils.findNumberOfDays(finalStartDate, item.endDate()));
                            }
                            else if (Utils.compareWithTwoDates(item.endDate(), finalReadingDate) > 0) {
                                return Math.toIntExact(Utils.findNumberOfDays(finalStartDate, finalReadingDate));
                            }
                            return Math.toIntExact(Utils.findNumberOfDays(finalStartDate, finalReadingDate));
                        }
                        else if (Utils.compareWithTwoDates(item.startDate(), finalStartDate) > 0) {
                            if (item.endDate() == null) {
                                return Math.toIntExact(Utils.findNumberOfDays(item.startDate(), finalReadingDate));
                            }
                            else if (Utils.compareWithTwoDates(item.endDate(), finalReadingDate) <= 0) {
                                return Math.toIntExact(Utils.findNumberOfDays(item.startDate(), item.endDate()));
                            }
                            else if (Utils.compareWithTwoDates(item.endDate(), finalReadingDate) > 0) {
                                return Math.toIntExact(Utils.findNumberOfDays(item.startDate(), finalReadingDate));
                            }
                            return Math.toIntExact(Utils.findNumberOfDays(item.startDate(), finalReadingDate));
                        }
                        return 0;
                    })
                    .sum();

            double consumption = 0.0;
            if (currentReadings == null) {
                consumption = readings.reading();
            }
            else {
                consumption =  readings.reading() - currentReadings.getCurrentReading();
            }

            double unitsPerDay = consumption / totalDays;

            List<CustomerIdRoomIdUnits> listCustomerIdRoomId = new ArrayList<>();
            listBeds.forEach(item -> {
                Date bedStartDate = null;
                Date bedEndDate = null;
                int noOfDays = 1;
                if (Utils.compareWithTwoDates(item.startDate(), finalStartDate) <= 0) {
                    if (item.endDate() == null) {
                        noOfDays = Math.toIntExact(Utils.findNumberOfDays(finalStartDate, finalReadingDate));
                        bedStartDate = finalStartDate;
                        bedEndDate = finalReadingDate;
                    }
                    else if (Utils.compareWithTwoDates(item.endDate(), finalReadingDate) <= 0) {
                        noOfDays = Math.toIntExact(Utils.findNumberOfDays(finalStartDate, item.endDate()));
                        bedStartDate = finalStartDate;
                        bedEndDate = item.endDate();
                    }
                    else if (Utils.compareWithTwoDates(item.endDate(), finalReadingDate) > 0) {
                        noOfDays = Math.toIntExact(Utils.findNumberOfDays(finalStartDate, finalReadingDate));
                        bedStartDate = finalStartDate;
                        bedEndDate = finalReadingDate;
                    }

                }
                else if (Utils.compareWithTwoDates(item.startDate(), finalStartDate) > 0) {
                    if (item.endDate() == null) {
                        noOfDays = Math.toIntExact(Utils.findNumberOfDays(item.startDate(), finalReadingDate));
                        bedStartDate = item.startDate();
                        bedEndDate = finalReadingDate;
                    }
                    else if (Utils.compareWithTwoDates(item.endDate(), finalReadingDate) <= 0) {
                        noOfDays = Math.toIntExact(Utils.findNumberOfDays(item.startDate(), item.endDate()));
                        bedStartDate = item.startDate();
                        bedEndDate = item.endDate();
                    }
                    else if (Utils.compareWithTwoDates(item.endDate(), finalReadingDate) > 0) {
                        noOfDays = Math.toIntExact(Utils.findNumberOfDays(item.startDate(), finalReadingDate));
                        bedStartDate = item.startDate();
                        bedEndDate = finalReadingDate;
                    }
                }

                CustomerIdRoomIdUnits customerIdRoomIdUnits = new CustomerIdRoomIdUnits(item.customerId(),
                        item.roomId(),
                        item.bedId(),
                        noOfDays * unitsPerDay,
                        bedStartDate,
                        bedEndDate);
                listCustomerIdRoomId.add(customerIdRoomIdUnits);

            });

            List<ElectricityReadings> listNewElectricityReading = new ArrayList<>();
            HashMap<Integer, Double> totalUnits= new HashMap<>();
            listCustomerIdRoomId.forEach(item -> {
                if (totalUnits.containsKey(item.roomId())) {
                    totalUnits.put(item.roomId(), totalUnits.get(item.roomId()) + item.units());
                }
                else {
                    totalUnits.put(item.roomId(), item.units());
                }
            });


            for (Integer key : totalUnits.keySet()) {
                com.smartstay.smartstay.dao.ElectricityReadings newReadings = new com.smartstay.smartstay.dao.ElectricityReadings();
                com.smartstay.smartstay.dao.ElectricityReadings roomPreviousReadings = electricityReadingRepository.getRoomCurrentReading(key);
                if (roomPreviousReadings != null) {
                    newReadings.setPreviousReading(roomPreviousReadings.getCurrentReading());
                    newReadings.setCurrentReading(roomPreviousReadings.getCurrentReading() + totalUnits.get(key));
                }
                else {
                    newReadings.setPreviousReading(0.0);
                    newReadings.setCurrentReading( totalUnits.get(key));
                }
                newReadings.setHostelId(hostelId);
                newReadings.setRoomId(key);
                newReadings.setCurrentUnitPrice(electricityConfig.getCharge());
                newReadings.setEntryDate(date);
                newReadings.setBillStatus(ElectricityBillStatus.INVOICE_NOT_GENERATED.name());
                newReadings.setFloorId(readings.floorId());
                newReadings.setConsumption(totalUnits.get(key));
                newReadings.setMissedEntry(false);
                newReadings.setCreatedAt(new Date());
                newReadings.setUpdatedAt(new Date());
                newReadings.setCreatedBy(authentication.getName());
                newReadings.setUpdatedBy(authentication.getName());
                newReadings.setBillStartDate(billStartDate);
                newReadings.setBillEndDate(billEndDate);

                listNewElectricityReading.add(newReadings);
            }

            List<com.smartstay.smartstay.dao.ElectricityReadings> listAddedReadingsAfterSavings = electricityReadingRepository.saveAll(listNewElectricityReading);

            listAddedReadingsAfterSavings.forEach(item -> {

             List<CustomersEbHistory> listEbHistory = listCustomerIdRoomId
                        .stream()
                        .filter(i -> i.roomId() == item.getRoomId())
                        .map(i -> {
                            CustomersEbHistory ebHistory = new CustomersEbHistory();
                            ebHistory.setReadingId(item.getId());
                            ebHistory.setCustomerId(i.customerId());
                            ebHistory.setRoomId(item.getRoomId());
                            ebHistory.setFloorId(item.getFloorId());
                            ebHistory.setBedId(i.bedId());
                            ebHistory.setUnits(i.units());
                            ebHistory.setAmount((i.units() * electricityConfig.getCharge()));
                            ebHistory.setStartDate(i.startDate());
                            ebHistory.setEndDate(i.endDate());
                            ebHistory.setCreatedAt(new Date());
                            ebHistory.setCreatedBy(ebHistory.getCreatedBy());

                            return ebHistory;
                        })
                        .toList();

                ebHistoryService.saveCustomerEb(listEbHistory);
            });

        }


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

            String startDate = "";
            String endDate = "";
            com.smartstay.smartstay.dao.ElectricityReadings ebReading = electricityReadingRepository.findTopByHostelIdOrderByEntryDateDesc(hostelId);
            if (ebReading != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(ebReading.getBillEndDate());

                startDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + electricityConfig.getBillDate();
                endDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
            }
            List<com.smartstay.smartstay.dto.electricity.ElectricityReadings> listElectricity = electricityReadingRepository.getElectricity(hostelId, startDate, endDate);
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

            boolean isHostelBased = false;
            if (electricityConfig.getTypeOfReading().equalsIgnoreCase(EBReadingType.ROOM_READING.name())) {
                isHostelBased = true;
            }
            else {
                isHostelBased = true;
            }


            ElectricityList list = new ElectricityList(isHostelBased, listUsages);

            return new ResponseEntity<>(list, HttpStatus.OK);


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
            List<Integer> roomIds = electricityReadingRepository.getRoomIds(hostelId);
            List<ElectricityCustomersList> electricityCustomersLists = ebHistoryService.getCustomerListFromRooms(roomIds);
            listCustomers.addAll(electricityCustomersLists.stream()
                    .map(item -> {
                        return new CustomersListMapper().apply(item);
                    })
                    .toList());


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

            listCustomers = ebHistoryService.getCustomerEbListForRoom(roomId);
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

        Date startDate = customers.getJoiningDate();
        Date endDate = new Date();



        List<ElectricityHistoryBySingleCustomer> listEbHistory =  ebHistoryService.getAllReadingByCustomerId(customerId, startDate, endDate);

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
