package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.CustomersBedHistory;
import com.smartstay.smartstay.dao.ElectricityConfig;
import com.smartstay.smartstay.dao.ElectricityReadings;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.electricity.MissedEbRooms;
import com.smartstay.smartstay.dto.electricity.PendingEbForSettlement;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class EbCalculationService {

    @Autowired
    private CustomersBedHistoryService customersBedHistoryService;
    @Autowired
    private HostelService hostelService;
//    private ElectricityService electricityService;
//    @Autowired
//    public void setElectricityService(@Lazy ElectricityService electricityService) {
//        this.electricityService = electricityService;
//    }

    public List<PendingEbForSettlement> calculateEbAmountAndUnit(String hostelId, String customerId, List<ElectricityReadings> pendingHistoryAmount, List<BedDetails> roomInfo) {
        ElectricityConfig electricityConfig = hostelService.getElectricityConfig(hostelId);

        List<PendingEbForSettlement> pendingAmount = new ArrayList<>();

        pendingHistoryAmount.forEach(i -> {
            AtomicReference<BedDetails> bedDetails = new AtomicReference<>();
            AtomicReference<Date> endDate = new AtomicReference<>(i.getBillEndDate());
            AtomicReference<Date> startDate = new AtomicReference<>(i.getBillStartDate());
            final AtomicLong[] totalNoOfPerson = {new AtomicLong()};
            List<CustomersBedHistory> customersList = customersBedHistoryService.getCustomersByRoomIdAndDates(i.getRoomId(), i.getBillStartDate(), i.getBillEndDate());
            if (customersList != null && !customersList.isEmpty()) {
                customersList
                        .forEach(item -> {
                            if (item.getCustomerId().equalsIgnoreCase(customerId)) {
                                if (item.getEndDate() != null) {
                                    if (Utils.compareWithTwoDates(item.getEndDate(), i.getBillEndDate()) <= 0) {
                                        endDate.set(item.getEndDate());
                                    }
                                }
                                else if (item.getEndDate() == null) {
                                    endDate.set(i.getBillEndDate());
                                }
                                if (Utils.compareWithTwoDates(item.getStartDate(), i.getBillStartDate()) <= 0) {
                                    startDate.set(i.getBillStartDate());
                                }
                                else {
                                    startDate.set(item.getStartDate());
                                }
                                bedDetails.set(roomInfo
                                        .stream()
                                        .filter(info -> info.getBedId().equals(item.getBedId()))
                                        .findFirst()
                                        .orElse(null));
                            }
                            if (Utils.compareWithTwoDates(item.getStartDate(), i.getBillStartDate()) <= 0 && item.getEndDate() == null) {
//                                totalNoOfPerson = totalNoOfPerson.get() + Utils.findNumberOfDays(i.getBillStartDate(), i.getBillEndDate());
                                totalNoOfPerson[0].set(totalNoOfPerson[0].get() + Utils.findNumberOfDays(i.getBillStartDate(), i.getBillEndDate()));
                            }
                            else if (Utils.compareWithTwoDates(item.getStartDate(), i.getBillStartDate()) <= 0 && item.getEndDate() != null) {
                                if (Utils.compareWithTwoDates(item.getEndDate(), i.getBillEndDate()) <=0) {
                                    //leaving date is before than end date
                                    totalNoOfPerson[0].set(totalNoOfPerson[0].get() + Utils.findNumberOfDays(i.getBillStartDate(), item.getEndDate()));
//                                    totalNoOfPerson[0] = totalNoOfPerson[0].get() + Utils.findNumberOfDays(i.getBillStartDate(), item.getEndDate());
                                }
                                else if (Utils.compareWithTwoDates(i.getBillEndDate(), item.getEndDate()) <= 0) {
                                    //leaving date is later than end date
                                    totalNoOfPerson[0].set(totalNoOfPerson[0].get() + Utils.findNumberOfDays(i.getBillStartDate(), i.getBillEndDate()));
//                                    totalNoOfPerson[0] = totalNoOfPerson[0].get() + Utils.findNumberOfDays(i.getBillStartDate(), item.getEndDate());
                                }
                            }
                            else if (Utils.compareWithTwoDates(item.getStartDate(), i.getBillStartDate()) > 0 && item.getEndDate() == null) {
//                                joining after the bill start date
                                totalNoOfPerson[0].set(totalNoOfPerson[0].get() + Utils.findNumberOfDays(item.getStartDate(), i.getBillEndDate()));
//                                totalNoOfPerson[0] = totalNoOfPerson[0].get() + Utils.findNumberOfDays(item.getStartDate(), i.getBillEndDate());
                            }
                            else if (Utils.compareWithTwoDates(item.getStartDate(), i.getBillStartDate()) > 0 && item.getEndDate() != null) {
                                if (Utils.compareWithTwoDates(item.getEndDate(), i.getBillEndDate()) <=0) {
                                    totalNoOfPerson[0].set(totalNoOfPerson[0].get() + Utils.findNumberOfDays(item.getStartDate(), item.getEndDate()));
//                                    totalNoOfPerson[0] = totalNoOfPerson[0].get() + Utils.findNumberOfDays(item.getStartDate(), item.getEndDate());
                                }
                                else if (Utils.compareWithTwoDates(i.getBillEndDate(), item.getEndDate()) <= 0) {
                                    totalNoOfPerson[0].set(totalNoOfPerson[0].get() + Utils.findNumberOfDays(item.getStartDate(), i.getBillEndDate()));
//                                    totalNoOfPerson[0] = totalNoOfPerson[0].get() + Utils.findNumberOfDays(item.getStartDate(), i.getBillEndDate());
                                }
                            }
                        });

                double unitsPerPersonPerDay = i.getConsumption() / totalNoOfPerson[0].get(); //per day
                long noOfDaysStayed = Utils.findNumberOfDays(startDate.get(), endDate.get());
                double totalUnitsPerPersion = unitsPerPersonPerDay * noOfDaysStayed;
                double unitPrice = 0;
                if (electricityConfig != null) {
                    unitPrice = electricityConfig.getCharge();
                }
                double price = totalUnitsPerPersion * unitPrice;
                PendingEbForSettlement pendingEbForSettlement = null;
                if (bedDetails.get() != null) {
                    pendingEbForSettlement = new PendingEbForSettlement(bedDetails.get().getRoomId(),
                            bedDetails.get().getRoomName(),
                            bedDetails.get().getBedName(),
                            bedDetails.get().getFloorName(),
                            totalUnitsPerPersion,
                            price,
                            Utils.dateToString(startDate.get()),
                            Utils.dateToString(endDate.get()));
                }

                pendingAmount.add(pendingEbForSettlement);
            }



//            MissedEbRooms missedEbRooms = new MissedEbRooms(bedDetails.get().getRoomId(),
//                    bedDetails.get().getRoomName(),
//                    bedDetails.get().getBedName(),
//                    bedDetails.get().getFloorName(),
//                    Utils.dateToString(startDate.get()),
//                    Utils.dateToString(endDate.get()));



        });

        return pendingAmount;
    }
}
