package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.Electricity.*;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dao.ElectricityReadings;
import com.smartstay.smartstay.dto.booking.BookedCustomer;
import com.smartstay.smartstay.dto.electricity.*;
import com.smartstay.smartstay.dto.electricity.HostelReadings;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.dto.room.RoomInfo;
import com.smartstay.smartstay.ennum.EBReadingType;
import com.smartstay.smartstay.ennum.ElectricityBillStatus;
import com.smartstay.smartstay.ennum.KycStatus;
import com.smartstay.smartstay.events.AddEbEvents;
import com.smartstay.smartstay.payloads.electricity.AddReading;
import com.smartstay.smartstay.payloads.electricity.UpdateElectricity;
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
import java.util.stream.Collectors;

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
    private CustomersBedHistoryService bedHistoryService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private HostelReadingsService hostelReadingsService;

    public ResponseEntity<?> addMeterReadingNew(String hostelId, AddReading readings) {
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

        BillingDates billingDates = hostelService.getBillingRuleOnDate(hostelId, date);

        ElectricityConfig electricityConfig1 = hostelService.getElectricityConfig(hostelId);
        if (electricityConfig1 == null) {
            return new ResponseEntity<>(Utils.ELECTRICITY_CONFIG_NOT_SET_UP, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Double previousReading = 0.0;
        if (date == null) {
            date = new Date();
        }


        Date billStartDate = new Date();
        Date billEndDate = new Date();

        if (billingDates != null ) {

            billStartDate = billingDates.currentBillStartDate();
            billEndDate = date;

        }

        if (electricityConfig1.getTypeOfReading().equalsIgnoreCase(EBReadingType.ROOM_READING.name())) {
            if (!Utils.checkNullOrEmpty(readings.roomId())) {
                return new ResponseEntity<>(Utils.INVALID_ROOM_ID, HttpStatus.BAD_REQUEST);
            }

            com.smartstay.smartstay.dao.ElectricityReadings electricityReadings = findLastEntryByRoomId(readings.roomId(), hostelId);
            boolean isFirstEntry = false;
            if (electricityReadings != null) {
                if (Utils.compareWithTwoDates(Utils.stringToDate(readings.readingDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT), electricityReadings.getEntryDate()) <=0) {
                    return new ResponseEntity<>(Utils.ALREADY_READING_TAKEN_THIS_DATE, HttpStatus.BAD_REQUEST);
                }
            }

            if (electricityReadings == null) {
                isFirstEntry = true;
                previousReading = 0.0;
            }
            else {
                previousReading = electricityReadings.getCurrentReading();
                billStartDate = Utils.addDaysToDate(electricityReadings.getEntryDate(), 1);
            }

            if (readings.reading() <= previousReading) {
                return new ResponseEntity<>(Utils.PREVIOUS_CURRENT_READING_NOT_MATCHING, HttpStatus.BAD_REQUEST);
            }

            com.smartstay.smartstay.dao.ElectricityReadings newReadings = new com.smartstay.smartstay.dao.ElectricityReadings();
            newReadings.setPreviousReading(previousReading);
            newReadings.setCurrentReading(readings.reading());
            newReadings.setHostelId(hostelId);
            newReadings.setRoomId(readings.roomId());
            newReadings.setCurrentUnitPrice(electricityConfig1.getCharge());
            newReadings.setEntryDate(date);
            newReadings.setBillStatus(ElectricityBillStatus.INVOICE_NOT_GENERATED.name());
            newReadings.setFloorId(readings.floorId());
            newReadings.setConsumption(readings.reading() - previousReading);
            newReadings.setMissedEntry(false);
            newReadings.setFirstEntry(isFirstEntry);
            newReadings.setCreatedAt(new Date());
            newReadings.setUpdatedAt(new Date());
            newReadings.setCreatedBy(users.getUserId());
            newReadings.setUpdatedBy(users.getUserId());
            newReadings.setBillStartDate(billStartDate);
            newReadings.setBillEndDate(billEndDate);

            com.smartstay.smartstay.dao.ElectricityReadings newReading = electricityReadingRepository.save(newReadings);
            if (!isFirstEntry) {
                eventPublisher.publishEvent(new AddEbEvents(this, hostelId, readings.roomId(), readings.reading(), electricityConfig1.getCharge(), date, users.getUserId(), electricityReadings, newReading.getId()));
            }
            return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
        }
        else {
            return addMeterReadingForHostelBasedNew(hostelId, readings, electricityConfig1, billingDates);
        }


    }


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

        BillingDates billingDates = hostelService.getBillingRuleOnDate(hostelId, date);

        ElectricityConfig electricityConfig1 = hostelService.getElectricityConfig(hostelId);
        if (electricityConfig1 == null) {
            return new ResponseEntity<>(Utils.ELECTRICITY_CONFIG_NOT_SET_UP, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Double previousReading = 0.0;
        if (date == null) {
            date = new Date();
        }


        Date billStartDate = new Date();
        Date billEndDate = new Date();

        if (billingDates != null ) {

            billStartDate = billingDates.currentBillStartDate();
            billEndDate = date;

        }

        if (electricityConfig1.getTypeOfReading().equalsIgnoreCase(EBReadingType.ROOM_READING.name())) {
            if (!Utils.checkNullOrEmpty(readings.roomId())) {
                return new ResponseEntity<>(Utils.INVALID_ROOM_ID, HttpStatus.BAD_REQUEST);
            }

            com.smartstay.smartstay.dao.ElectricityReadings electricityReadings = findLastEntryByRoomId(readings.roomId(), hostelId);
            boolean isFirstEntry = false;
            if (electricityReadings != null) {
                if (Utils.compareWithTwoDates(Utils.stringToDate(readings.readingDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT), electricityReadings.getEntryDate()) <=0) {
                    return new ResponseEntity<>(Utils.ALREADY_READING_TAKEN_THIS_DATE, HttpStatus.BAD_REQUEST);
                }
            }

            if (electricityReadings == null) {
                isFirstEntry = true;
                previousReading = 0.0;
            }
            else {
                previousReading = electricityReadings.getCurrentReading();
                billStartDate = Utils.addDaysToDate(electricityReadings.getEntryDate(), 1);
            }

            if (readings.reading() <= previousReading) {
                return new ResponseEntity<>(Utils.PREVIOUS_CURRENT_READING_NOT_MATCHING, HttpStatus.BAD_REQUEST);
            }

            com.smartstay.smartstay.dao.ElectricityReadings newReadings = new com.smartstay.smartstay.dao.ElectricityReadings();
            newReadings.setPreviousReading(previousReading);
            newReadings.setCurrentReading(readings.reading());
            newReadings.setHostelId(hostelId);
            newReadings.setRoomId(readings.roomId());
            newReadings.setCurrentUnitPrice(electricityConfig1.getCharge());
            newReadings.setEntryDate(date);
            newReadings.setBillStatus(ElectricityBillStatus.INVOICE_NOT_GENERATED.name());
            newReadings.setFloorId(readings.floorId());
            newReadings.setConsumption(readings.reading() - previousReading);
            newReadings.setMissedEntry(false);
            newReadings.setFirstEntry(isFirstEntry);
            newReadings.setCreatedAt(new Date());
            newReadings.setUpdatedAt(new Date());
            newReadings.setCreatedBy(users.getUserId());
            newReadings.setUpdatedBy(users.getUserId());
            newReadings.setBillStartDate(billStartDate);
            newReadings.setBillEndDate(billEndDate);

            electricityReadingRepository.save(newReadings);

            return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
        }
        else {
            return hostelReadingsService.addEbReadings(hostelId, readings, billStartDate, billEndDate, electricityConfig1);
        }
    }

    public ResponseEntity<?> addMeterReadingForHostelBasedNew(String hostelId, AddReading readings, ElectricityConfig electricityConfig, BillingDates billingDates) {

        Date date = null;
        Date billStartDate = new Date();
        Date billEndDate = new Date();
        boolean isProRate = true;
        double totalBillAmount = 0.0;
        boolean isFirstEntry = false;

        CurrentReadings currentReadings = electricityReadingRepository.getCurrentReadings(hostelId);
        List<CurrentReadings> listCurrentReadings = electricityReadingRepository.getAllCurrentReadings(hostelId);
        if (listCurrentReadings != null) {
            double sumOfPreviousReadings = listCurrentReadings
                    .stream()
                    .mapToDouble(CurrentReadings::currentReading)
                    .sum();
            if (currentReadings != null && currentReadings.entryDate() != null) {
                Date entryDate = currentReadings.entryDate();
                currentReadings = new CurrentReadings(sumOfPreviousReadings, entryDate);
            }
        }

        if (readings.readingDate() != null) {
            date = Utils.stringToDate(readings.readingDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            if (Utils.compareWithTwoDates(new Date(), date) < 0) {
                return new ResponseEntity<>(Utils.FUTURE_DATES_NOT_ALLOWED, HttpStatus.BAD_REQUEST);
            }
            if (currentReadings != null) {
                if (Utils.compareWithTwoDates(Utils.stringToDate(readings.readingDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT), currentReadings.entryDate()) <=0) {
                    return new ResponseEntity<>(Utils.ALREADY_READING_TAKEN_THIS_DATE, HttpStatus.BAD_REQUEST);
                }

                Calendar calBillStart = Calendar.getInstance();
                calBillStart.setTime(currentReadings.entryDate());
                calBillStart.add(Calendar.DAY_OF_MONTH, 1);

                billStartDate = calBillStart.getTime();

            }

            if (billingDates != null) {

                if (currentReadings == null) {

                    billStartDate = billingDates.currentBillStartDate();
                }

                billEndDate = date;
                isProRate = electricityConfig.isProRate();
            }

        }

        Double previousReading = 0.0;
        if (date == null) {
            date = new Date();
        }


        if (currentReadings == null) {
            isFirstEntry = true;
            previousReading = 0.0;
        }
        else {
            previousReading = currentReadings.currentReading();
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
                    newReadings.setCurrentReading(Utils.roundOffWithTwoDigit((unitPerPerson * personCountPerRoom.get(key))));
                }
                else {
                    newReadings.setPreviousReading(roomPreviousReadings.getCurrentReading());
                    newReadings.setCurrentReading(Utils.roundOffWithTwoDigit((unitPerPerson * personCountPerRoom.get(key)) + roomPreviousReadings.getCurrentReading()));
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
                newReadings.setFirstEntry(isFirstEntry);
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
                isFirstEntry = false;
                Calendar cal = Calendar.getInstance();
                cal.setTime(currentReadings.entryDate());
                cal.add(Calendar.DAY_OF_MONTH, 1);

                startDate = cal.getTime();
            }
            else {
                isFirstEntry = true;
                Calendar cal = Calendar.getInstance();
                cal.setTime(readingDate);
                assert electricityConfig != null;
                cal.set(Calendar.DAY_OF_MONTH, electricityConfig.getBillDate());
                cal.add(Calendar.DAY_OF_MONTH, 1);

                startDate = cal.getTime();


                Calendar calEndDate = Calendar.getInstance();
                calEndDate.setTime(readingDate);

                endDate = calEndDate.getTime();

            }
            Date finalStartDate = startDate;
            Date finalReadingDate = endDate;

            List<CustomerIdRoomIdUnits> listCustomerIdRoomId = new ArrayList<>();


            CurrentReadings finalCurrentReadings = currentReadings;
            int totalDays = listBeds.stream()
                    .mapToInt(item -> {
                        assert finalCurrentReadings != null;
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
                consumption =  readings.reading() - currentReadings.currentReading();
            }

            double unitsPerDay = consumption / totalDays;


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
                    newReadings.setCurrentReading(Utils.roundOffWithTwoDigit(roomPreviousReadings.getCurrentReading() + totalUnits.get(key)));
                }
                else {
                    newReadings.setPreviousReading(0.0);
                    newReadings.setCurrentReading(Utils.roundOffWithTwoDigit(totalUnits.get(key)));
                }
                newReadings.setHostelId(hostelId);
                newReadings.setRoomId(key);
                newReadings.setCurrentUnitPrice(electricityConfig.getCharge());
                newReadings.setEntryDate(date);
                newReadings.setBillStatus(ElectricityBillStatus.INVOICE_NOT_GENERATED.name());
                newReadings.setFloorId(readings.floorId());
                newReadings.setConsumption(Utils.roundOffWithTwoDigit(totalUnits.get(key)));
                newReadings.setMissedEntry(false);
                newReadings.setCreatedAt(new Date());
                newReadings.setUpdatedAt(new Date());
                newReadings.setCreatedBy(authentication.getName());
                newReadings.setUpdatedBy(authentication.getName());
                newReadings.setBillStartDate(billStartDate);
                newReadings.setBillEndDate(billEndDate);
                newReadings.setFirstEntry(isFirstEntry);
                listNewElectricityReading.add(newReadings);
            }
            List<com.smartstay.smartstay.dao.ElectricityReadings> listAddedReadingsAfterSavings = new ArrayList<>();
            if (!totalUnits.isEmpty()) {
                listAddedReadingsAfterSavings = electricityReadingRepository.saveAll(listNewElectricityReading);
            }
            else {
                List<Rooms> listRooms = roomsService.getAllRoomsByHostelId(hostelId);

                Double currentReadingForNoOccupants = (readings.reading() - previousReading) / listRooms.size();

                Date finalDate = date;
                Date finalBillStartDate1 = billStartDate;
                Date finalBillEndDate1 = billEndDate;
                boolean finalIsFirstEntry = isFirstEntry;
                listRooms.forEach(item -> {
                    com.smartstay.smartstay.dao.ElectricityReadings newReadings = new com.smartstay.smartstay.dao.ElectricityReadings();
                    com.smartstay.smartstay.dao.ElectricityReadings roomPreviousReadings = electricityReadingRepository.getRoomCurrentReading(item.getRoomId());
                    if (roomPreviousReadings != null) {
                        newReadings.setPreviousReading(Utils.roundOffWithTwoDigit(roomPreviousReadings.getCurrentReading()));
                        newReadings.setCurrentReading(Utils.roundOffWithTwoDigit(roomPreviousReadings.getCurrentReading() + currentReadingForNoOccupants));
                    }
                    else {
                        newReadings.setPreviousReading(0.0);
                        newReadings.setCurrentReading(currentReadingForNoOccupants);
                    }

                    newReadings.setHostelId(hostelId);
                    newReadings.setRoomId(item.getRoomId());
                    newReadings.setCurrentUnitPrice(electricityConfig.getCharge());
                    newReadings.setEntryDate(finalDate);
                    newReadings.setBillStatus(ElectricityBillStatus.INVOICE_NOT_GENERATED.name());
                    newReadings.setFloorId(readings.floorId());
                    newReadings.setConsumption(currentReadingForNoOccupants);
                    newReadings.setMissedEntry(false);
                    newReadings.setCreatedAt(new Date());
                    newReadings.setUpdatedAt(new Date());
                    newReadings.setCreatedBy(authentication.getName());
                    newReadings.setUpdatedBy(authentication.getName());
                    newReadings.setBillStartDate(finalBillStartDate1);
                    newReadings.setBillEndDate(finalBillEndDate1);
                    newReadings.setFirstEntry(finalIsFirstEntry);
                    listNewElectricityReading.add(newReadings);
                });

                electricityReadingRepository.saveAll(listNewElectricityReading);
            }


            if (!isFirstEntry) {
                listAddedReadingsAfterSavings.forEach(item -> {

                    List<CustomersEbHistory> listEbHistory = listCustomerIdRoomId
                            .stream()
                            .filter(i -> Objects.equals(i.roomId(), item.getRoomId()))
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

            String startDate;
            String endDate;
            com.smartstay.smartstay.dao.ElectricityReadings ebReading = electricityReadingRepository.findTopByHostelIdOrderByEntryDateDesc(hostelId);
            if (ebReading != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(ebReading.getBillEndDate());

                startDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + electricityConfig.getBillDate();
                endDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
            } else {
                endDate = "";
                startDate = "";
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

            List<RoomInfoForEB> listBedForEb = roomsService.getBedsNotRegisteredOnEB(listRoomsInMeterReadings, hostelId, startDate, endDate);
            if (!listBedForEb.isEmpty()) {
                List<ElectricityUsage> usages =  listBedForEb.stream()
                        .map(item -> new ElectricityUsageMapper(startDate, endDate).apply(item))
                        .toList();

                listUsages.addAll(usages);

            }

            boolean isHostelBased = false;
            Double lastReading = 0.0;
            if (electricityConfig.getTypeOfReading().equalsIgnoreCase(EBReadingType.HOSTEL_READING.name())) {
                isHostelBased = true;
                Double lReading = electricityReadingRepository.getLastReading(hostelId);
                if (lReading != null) {
                    lastReading = Utils.roundOfDouble(lReading);
                }
                else {
                    lastReading = 0.0;
                }
            }


            ElectricityList list = new ElectricityList(hostelId, lastReading, isHostelBased, listUsages);

            return new ResponseEntity<>(list, HttpStatus.OK);


    }

    public ResponseEntity<?> getEbReadingsNew(String hostelId) {
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
        List<ElectricityReadings> latestReadings = electricityReadingRepository.getLatestReadingOfAllRooms(hostelId);

        List<BedHistoryByRoomId> listBeds = null;
        HashMap<Integer, Integer> occupantsCounts;
        List<RoomInfo> listRoomDetails = new ArrayList<>();
        List<HostelReadings> listHostelReadings = null;
        boolean isHostelBased = false;

        if (latestReadings != null) {
            listBeds = latestReadings
                    .stream()
                    .map(i -> new BedHistoryByRoomId(i.getRoomId(), i.getBillStartDate(), i.getBillEndDate()))
                    .toList();
            List<Integer> roomIds = latestReadings.stream()
                    .map(ElectricityReadings::getRoomId)
                    .toList();
            listRoomDetails = roomsService.getRoom(roomIds);
        }

        if (listBeds != null) {
            occupantsCounts = bedHistoryService.findByStartAndEndDateAndRoomIds(listBeds);
        } else {
            occupantsCounts = null;
        }

        Double lastReading = 0.0;
        if (electricityConfig.getTypeOfReading().equalsIgnoreCase(EBReadingType.HOSTEL_READING.name())) {
            isHostelBased = true;
            Double lReading = electricityReadingRepository.getLastReading(hostelId);
            if (lReading != null) {
                lastReading = Utils.roundOfDouble(lReading);
            }
            else {
                lastReading = 0.0;
            }

            listHostelReadings = hostelReadingsService.getLastReading(hostelId);

        }

        List<Integer> listRoomsInMeterReadings = latestReadings
                .stream()
                .map(ElectricityReadings::getRoomId)
                .toList();
        List<RoomInfo> listRoomInfo = roomsService.getRoom(listRoomsInMeterReadings);

        List<ElectricityUsage> listUsages = new ArrayList<>(latestReadings
                .stream()
                .map(item -> {
                    return new ElectricityUsgeMapper(occupantsCounts, listRoomInfo).apply(item);
                })
                .toList());

        List<RoomInfoForEB> listBedForEb = roomsService.getBedsNotRegisteredOnEB(listRoomsInMeterReadings, hostelId);
        if (!listBedForEb.isEmpty()) {
            List<ElectricityUsage> usages =  listBedForEb.stream()
                    .map(item -> new ElectricityUsageMapper(null, null).apply(item))
                    .toList();

            listUsages.addAll(usages);

        }
        if (electricityConfig.getTypeOfReading().equalsIgnoreCase(EBReadingType.ROOM_READING.name())) {
            ElectricityList list = new ElectricityList(hostelId, lastReading, isHostelBased, listUsages);

            return new ResponseEntity<>(list, HttpStatus.OK);
        }
        else {
//            ElectricityList list = new ElectricityList(hostelId, lastReading, isHostelBased, listUsages);
            ElectricityListHostelReadings list = new ElectricityListHostelReadings(hostelId,
                    lastReading,
                    isHostelBased,
                    listUsages,
                    listHostelReadings);

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
        List<ElectricityReadings> listElectricities = electricityReadingRepository.getRoomReading(roomId);

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



        RoomUsages roomUsages = new RoomUsages(roomInfo, hostelId, roomList, listCustomers);

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

    public List<ElectricityReadings> getAllElectricityReadingForRecurring(String hostelId, Date startDate, Date endDate) {
        return electricityReadingRepository.listAllReadingsForGenerateInvoice(hostelId, startDate, endDate);
    }

    public void markAsInvoiceGenerated(List<ElectricityReadings> listReadingForMakingInvoiceGenerated) {
        electricityReadingRepository.saveAll(listReadingForMakingInvoiceGenerated);
    }

    public Double getPreviousMonthEbAmount(String hostelId) {
        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);
        if (billingDates != null) {
            Calendar calStartDate = Calendar.getInstance();
            calStartDate.setTime(billingDates.currentBillStartDate());
            calStartDate.add(Calendar.MONTH, -1);


            Calendar calEndDate = Calendar.getInstance();
            calEndDate.setTime(billingDates.currentBillEndDate());
            calEndDate.add(Calendar.MONTH, -1);

            List<ElectricityReadings> listReadings = electricityReadingRepository.findByBetweenDates(hostelId, calStartDate.getTime(), calEndDate.getTime());

            return listReadings
                    .stream()
                    .mapToDouble(i -> {
                        Double consumption = i.getConsumption();
                        double currentPrice = i.getCurrentUnitPrice();

                        if (i.isFirstEntry()) {
                            consumption = 0.0;
                        }

                        return Math.round(consumption*currentPrice);
                    })
                    .sum();

        }
        else {
            return 0.0;
        }
    }

    public ResponseEntity<?> updateEBReadings(String hostelId, String readingId, UpdateElectricity updateElectricity) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_ELECTRIC_CITY, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!Utils.checkNullOrEmpty(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }
        if (updateElectricity == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (updateElectricity.reading() == null && updateElectricity.entryDate() == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        if (updateElectricity.entryDate() != null) {
            Date entryDate = Utils.stringToDate(updateElectricity.entryDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            if (Utils.compareWithTwoDates(entryDate, new Date()) > 0) {
                return new ResponseEntity<>(Utils.FUTURE_DATES_NOT_ALLOWED, HttpStatus.BAD_REQUEST);
            }
        }

        ElectricityConfig electricityConfig = hostelService.getElectricityConfig(hostelId);
        if (electricityConfig == null) {
            return new ResponseEntity<>(Utils.ELECTRICITY_CONFIG_NOT_SET_UP, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (electricityConfig.getTypeOfReading().equalsIgnoreCase(EBReadingType.ROOM_READING.name())) {
            Integer id = Integer.parseInt(readingId);

            ElectricityReadings currentReading = electricityReadingRepository.getReferenceById(id);
            if (currentReading == null) {
                return new ResponseEntity<>(Utils.EB_ENTRY_NOT_FOUND, HttpStatus.BAD_REQUEST);
            }
            if (currentReading.getBillStatus().equalsIgnoreCase(ElectricityBillStatus.INVOICE_GENERATED.name())) {
                return new ResponseEntity<>(Utils.EB_ENTRY_CANNOT_CHANGE_INVOICE_GENERATED, HttpStatus.BAD_REQUEST);
            }
            ElectricityReadings previousReading = electricityReadingRepository.getPreviousEntry(currentReading.getRoomId(), id);
            if (previousReading != null) {
                if (updateElectricity.reading() != null) {
                    double consumption = 0.0;
                    if (updateElectricity.reading() <= previousReading.getCurrentReading()) {
                        return new ResponseEntity<>(Utils.PREVIOUS_CURRENT_READING_NOT_MATCHING, HttpStatus.BAD_REQUEST);
                    }
                    consumption = updateElectricity.reading() - previousReading.getCurrentReading();
                    currentReading.setCurrentReading(updateElectricity.reading());
                    currentReading.setConsumption(consumption);
                }
                if (updateElectricity.entryDate() != null) {
                    Date entryDate = Utils.stringToDate(updateElectricity.entryDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
                    if (Utils.compareWithTwoDates(entryDate, previousReading.getEntryDate()) <= 0) {
                        return new ResponseEntity<>(Utils.ALREADY_READING_TAKEN_THIS_DATE, HttpStatus.BAD_REQUEST);
                    }
                    currentReading.setEntryDate(entryDate);
                    currentReading.setBillEndDate(entryDate);
                }
                currentReading.setUpdatedBy(authentication.getName());
                currentReading.setUpdatedAt(new Date());
                electricityReadingRepository.save(currentReading);
            }
            else {
                if (updateElectricity.reading() != null) {
                    currentReading.setCurrentReading(updateElectricity.reading());
                    currentReading.setConsumption(updateElectricity.reading());
                }
                if (updateElectricity.entryDate() != null) {
                    Date entryDate = Utils.stringToDate(updateElectricity.entryDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
                    BillingDates billingDates = hostelService.getBillingRuleOnDate(hostelId, entryDate);
                    if (billingDates != null) {
                        currentReading.setBillStartDate(billingDates.currentBillStartDate());
                    }
                    currentReading.setEntryDate(entryDate);
                    currentReading.setBillEndDate(entryDate);

                }
                currentReading.setUpdatedAt(new Date());
                currentReading.setUpdatedBy(authentication.getName());
                electricityReadingRepository.save(currentReading);
            }
            return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
        }
        else {
            return hostelReadingsService.updateEbReading(hostelId, readingId, updateElectricity);
        }

    }

    public ResponseEntity<?> deleteReading(String hostelId, String readingId) {
        int id = 0;
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_ELECTRIC_CITY, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!Utils.checkNullOrEmpty(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }
        try {
            id = Integer.parseInt(readingId);
        }
        catch (Exception e) {
            return new ResponseEntity<>(Utils.INVALID_READING_ID, HttpStatus.BAD_REQUEST);
        }
        if (!Utils.checkNullOrEmpty(id)) {
            return new ResponseEntity<>(Utils.INVALID_READING_ID, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        ElectricityConfig electricityConfig = hostelService.getElectricityConfig(hostelId);
        if (electricityConfig == null) {
            return new ResponseEntity<>(Utils.ELECTRICITY_CONFIG_NOT_SET_UP, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (electricityConfig.getTypeOfReading().equalsIgnoreCase(EBReadingType.ROOM_READING.name())) {
            ElectricityReadings er = electricityReadingRepository.findById(id).orElse(null);
            if (er == null) {
                return new ResponseEntity<>(Utils.INVALID_READING_ID, HttpStatus.BAD_REQUEST);
            }
            ElectricityReadings latestEntry = electricityReadingRepository.findTopByRoomIdAndHostelIdOrderByEntryDateDesc(er.getRoomId(), hostelId);
            if (!latestEntry.getId().equals(er.getId())) {
                return new ResponseEntity<>(Utils.DELETE_AVAILABLE_ONLY_FOR_LAST_ENTRY, HttpStatus.BAD_REQUEST);
            }
            if (er.getBillStatus().equalsIgnoreCase(ElectricityBillStatus.INVOICE_GENERATED.name())) {
                return new ResponseEntity<>(Utils.EB_ENTRY_CANNOT_DELETE_INVOICE_GENERATED, HttpStatus.BAD_REQUEST);
            }
            electricityReadingRepository.delete(er);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        else {
            return hostelReadingsService.deleteLatestEntry(hostelId, readingId);
        }

    }
}
