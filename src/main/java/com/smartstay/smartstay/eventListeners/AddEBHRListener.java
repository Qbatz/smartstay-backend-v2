package com.smartstay.smartstay.eventListeners;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.electricity.CustomerBedsList;
import com.smartstay.smartstay.dto.electricity.CustomerIdRoomIdUnits;
import com.smartstay.smartstay.ennum.ElectricityBillStatus;
import com.smartstay.smartstay.events.HostelReadingEbEvents;
import com.smartstay.smartstay.services.*;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AddEBHRListener {

    @Autowired
    private CustomersService customerService;
    @Autowired
    private ElectricityService electricityService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private RoomsService roomsService;
    @Autowired
    private CustomerEbHistoryService ebHistoryService;

    @Async
    @EventListener
    public void addEbInfoForHostel(HostelReadingEbEvents events) {


        HostelReadings hr = events.getHostelReadings();
        Date billStartDate;
        Date billEndDate;
        String hostelId;
        double consumption = 0;
        if (hr != null) {
            billStartDate = hr.getBillStartDate();
            billEndDate = hr.getBillEndDate();
            consumption = hr.getConsumption();
            hostelId = hr.getHostelId();
        } else {
            hostelId = null;
            billEndDate = new Date();
            billStartDate = new Date();
        }
        ElectricityConfig electricityConfig = hostelService.getElectricityConfig(hostelId);
        List<CustomerBedsList> listBeds =  customerService.getCustomersFromBedHistory(hostelId, billStartDate, billEndDate);

        List<CustomerIdRoomIdUnits> listCustomerIdRoomId = new ArrayList<>();
        int totalDays = listBeds.stream()
                .mapToInt(item -> {
                    if (Utils.compareWithTwoDates(item.startDate(), billStartDate) <= 0) {
                        if (item.endDate() == null) {
                            return Math.toIntExact(Utils.findNumberOfDays(billStartDate, billEndDate));
                        }
                        else if (Utils.compareWithTwoDates(item.endDate(), billEndDate) <= 0) {
                            return Math.toIntExact(Utils.findNumberOfDays(billStartDate, item.endDate()));
                        }
                        else if (Utils.compareWithTwoDates(item.endDate(), billEndDate) > 0) {
                            return Math.toIntExact(Utils.findNumberOfDays(billStartDate, billEndDate));
                        }
                        return Math.toIntExact(Utils.findNumberOfDays(billStartDate, billEndDate));
                    }
                    else if (Utils.compareWithTwoDates(item.startDate(), billStartDate) > 0) {
                        if (item.endDate() == null) {
                            return Math.toIntExact(Utils.findNumberOfDays(item.startDate(), billEndDate));
                        }
                        else if (Utils.compareWithTwoDates(item.endDate(), billEndDate) <= 0) {
                            return Math.toIntExact(Utils.findNumberOfDays(item.startDate(), item.endDate()));
                        }
                        else if (Utils.compareWithTwoDates(item.endDate(), billEndDate) > 0) {
                            return Math.toIntExact(Utils.findNumberOfDays(item.startDate(), billEndDate));
                        }
                        return Math.toIntExact(Utils.findNumberOfDays(item.startDate(), billEndDate));
                    }
                    return 0;
                })
                .sum();

        double unitsPerDay = consumption / totalDays;

        listBeds.forEach(item -> {
            Date bedStartDate = null;
            Date bedEndDate = null;
            int noOfDays = 1;
            if (Utils.compareWithTwoDates(item.startDate(), billStartDate) <= 0) {
                if (item.endDate() == null) {
                    noOfDays = Math.toIntExact(Utils.findNumberOfDays(billStartDate, billEndDate));
                    bedStartDate = billStartDate;
                    bedEndDate = billEndDate;
                }
                else if (Utils.compareWithTwoDates(item.endDate(), billEndDate) <= 0) {
                    noOfDays = Math.toIntExact(Utils.findNumberOfDays(billStartDate, item.endDate()));
                    bedStartDate = billStartDate;
                    bedEndDate = item.endDate();
                }
                else if (Utils.compareWithTwoDates(item.endDate(), billEndDate) > 0) {
                    noOfDays = Math.toIntExact(Utils.findNumberOfDays(billStartDate, billEndDate));
                    bedStartDate = billStartDate;
                    bedEndDate = billEndDate;
                }

            }
            else if (Utils.compareWithTwoDates(item.startDate(), billStartDate) > 0) {
                if (item.endDate() == null) {
                    noOfDays = Math.toIntExact(Utils.findNumberOfDays(item.startDate(), billEndDate));
                    bedStartDate = item.startDate();
                    bedEndDate = billEndDate;
                }
                else if (Utils.compareWithTwoDates(item.endDate(), billEndDate) <= 0) {
                    noOfDays = Math.toIntExact(Utils.findNumberOfDays(item.startDate(), item.endDate()));
                    bedStartDate = item.startDate();
                    bedEndDate = item.endDate();
                }
                else if (Utils.compareWithTwoDates(item.endDate(), billEndDate) > 0) {
                    noOfDays = Math.toIntExact(Utils.findNumberOfDays(item.startDate(), billEndDate));
                    bedStartDate = item.startDate();
                    bedEndDate = billEndDate;
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
            com.smartstay.smartstay.dao.ElectricityReadings roomPreviousReadings = electricityService.getRoomCurrentReading(key);
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
            newReadings.setEntryDate(billEndDate);
            newReadings.setBillStatus(ElectricityBillStatus.INVOICE_NOT_GENERATED.name());
            newReadings.setFloorId(null);
            newReadings.setConsumption(Utils.roundOffWithTwoDigit(totalUnits.get(key)));
            newReadings.setMissedEntry(false);
            newReadings.setCreatedAt(new Date());
            newReadings.setUpdatedAt(new Date());
            newReadings.setCreatedBy(hr.getCreatedBy());
            newReadings.setUpdatedBy(hr.getCreatedBy());
            newReadings.setBillStartDate(billStartDate);
            newReadings.setBillEndDate(billEndDate);
            newReadings.setFirstEntry(false);
            listNewElectricityReading.add(newReadings);
        }

        List<com.smartstay.smartstay.dao.ElectricityReadings> listAddedReadingsAfterSavings = new ArrayList<>();
        if (!totalUnits.isEmpty()) {
            listAddedReadingsAfterSavings = electricityService.saveAll(listNewElectricityReading);
        }
        else {
            List<Rooms> listRooms = roomsService.getAllRoomsByHostelId(hostelId);

            Double currentReadingForNoOccupants = consumption / listRooms.size();

            Date finalBillStartDate1 = billStartDate;
            Date finalBillEndDate1 = billEndDate;
            boolean finalIsFirstEntry = hr.isFirstEntry();
            listRooms.forEach(item -> {
                com.smartstay.smartstay.dao.ElectricityReadings newReadings = new com.smartstay.smartstay.dao.ElectricityReadings();
                com.smartstay.smartstay.dao.ElectricityReadings roomPreviousReadings = electricityService.getRoomCurrentReading(item.getRoomId());
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
                newReadings.setEntryDate(billEndDate);
                newReadings.setBillStatus(ElectricityBillStatus.INVOICE_NOT_GENERATED.name());
                newReadings.setFloorId(item.getFloorId());
                newReadings.setConsumption(currentReadingForNoOccupants);
                newReadings.setMissedEntry(false);
                newReadings.setCreatedAt(new Date());
                newReadings.setUpdatedAt(new Date());
                newReadings.setCreatedBy(hr.getCreatedBy());
                newReadings.setUpdatedBy(hr.getCreatedBy());
                newReadings.setBillStartDate(finalBillStartDate1);
                newReadings.setBillEndDate(finalBillEndDate1);
                newReadings.setFirstEntry(finalIsFirstEntry);
                listNewElectricityReading.add(newReadings);
            });

            electricityService.saveAll(listNewElectricityReading);
        }

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
