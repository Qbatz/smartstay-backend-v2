package com.smartstay.smartstay.eventListeners;

import com.smartstay.smartstay.dao.CustomersBedHistory;
import com.smartstay.smartstay.dao.CustomersEbHistory;
import com.smartstay.smartstay.dao.ElectricityConfig;
import com.smartstay.smartstay.dao.ElectricityReadings;
import com.smartstay.smartstay.dto.booking.BookedCustomerInfoElectricity;
import com.smartstay.smartstay.events.AddEbEvents;
import com.smartstay.smartstay.services.*;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AddEbEventListeners {

    @Autowired
    private ElectricityService electricityService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private BookingsService bookingService;
    @Autowired
    private CustomerEbHistoryService ebHistoryService;
    @Autowired
    private CustomersBedHistoryService customerBedHistory;

    @Async
    @EventListener
    public void addEbInfoForRoom(AddEbEvents ebEvents) {
        ElectricityConfig electricityConfig = hostelService.getElectricityConfig(ebEvents.getHostelId());
        ElectricityReadings electricityReadings = ebEvents.getElectricityReadings();

        Double currentConsumption = electricityReadings.getConsumption();

        Date electricityStartDate = electricityReadings.getBillStartDate();


        Date electricityEndDate = electricityReadings.getEntryDate();

        List<CustomersBedHistory> listCustomerBedHistory = customerBedHistory.getCustomersByRoomIdAndDates(ebEvents.getRoomId(), electricityStartDate, electricityEndDate);
        if (!listCustomerBedHistory.isEmpty()) {
            long personCount = listCustomerBedHistory
                    .stream()
                    .filter(item -> Utils.compareWithTwoDates(item.getStartDate(), electricityStartDate) <= 0 && (item.getEndDate() == null || Utils.compareWithTwoDates(electricityEndDate, item.getEndDate()) <= 0)).count();

            if (listCustomerBedHistory.size() == personCount) {
//                long noOfDaysBetweenStartAndEndDate = Utils.findNumberOfDays(startDate, endDate);
                double finalUnitsPerPerson =  currentConsumption / listCustomerBedHistory.size();
                double finalAmount = electricityConfig.getCharge() * finalUnitsPerPerson;

                List<CustomersEbHistory> listEbHistory = listCustomerBedHistory
                        .stream()
                        .map(item -> {
                            CustomersEbHistory ebHistory = new CustomersEbHistory();
                            ebHistory.setReadingId(electricityReadings.getId());
                            ebHistory.setCustomerId(item.getCustomerId());
                            ebHistory.setRoomId(item.getRoomId());
                            ebHistory.setFloorId(item.getFloorId());
                            ebHistory.setBedId(item.getBedId());
                            ebHistory.setUnits(finalUnitsPerPerson);
                            ebHistory.setAmount(finalAmount);
                            ebHistory.setStartDate(electricityStartDate);
                            ebHistory.setEndDate(electricityEndDate);
                            ebHistory.setCreatedAt(new Date());
                            ebHistory.setCreatedBy(ebHistory.getCreatedBy());

                            return ebHistory;
                        })
                        .toList();

                ebHistoryService.addEbForCustomer(listEbHistory);
            }
            else {
                AtomicLong totalNoOfPerson = new AtomicLong();
                listCustomerBedHistory
                        .forEach(item -> {
                            if (Utils.compareWithTwoDates(item.getStartDate(), electricityStartDate) <= 0 && item.getEndDate() == null) {
                                totalNoOfPerson.set(totalNoOfPerson.get() + Utils.findNumberOfDays(electricityStartDate, electricityEndDate));
                            }
                            else if (Utils.compareWithTwoDates(item.getStartDate(), electricityStartDate) <= 0 && item.getEndDate() != null) {
                                if (Utils.compareWithTwoDates(item.getEndDate(), electricityEndDate) <=0) {
                                    //leaving date is before than end date
                                    totalNoOfPerson.set(totalNoOfPerson.get() + Utils.findNumberOfDays(electricityStartDate, item.getEndDate()));
                                }
                                else if (Utils.compareWithTwoDates(electricityEndDate, item.getEndDate()) <= 0) {
                                    //leaving date is later than end date
                                    totalNoOfPerson.set(totalNoOfPerson.get() + Utils.findNumberOfDays(electricityStartDate, electricityEndDate));
                                }
                            }
                            else if (Utils.compareWithTwoDates(item.getStartDate(), electricityStartDate) > 0 && item.getEndDate() == null) {
                                totalNoOfPerson.set(totalNoOfPerson.get() + Utils.findNumberOfDays(item.getStartDate(), electricityEndDate));
                            }
                            else if (Utils.compareWithTwoDates(item.getStartDate(), electricityStartDate) > 0 && item.getEndDate() != null) {
                                if (Utils.compareWithTwoDates(item.getEndDate(), electricityEndDate) <=0) {
                                    totalNoOfPerson.set(totalNoOfPerson.get() + Utils.findNumberOfDays(item.getStartDate(), item.getEndDate()));
                                }
                                else if (Utils.compareWithTwoDates(electricityStartDate, item.getEndDate()) <= 0) {
                                    totalNoOfPerson.set(totalNoOfPerson.get() + Utils.findNumberOfDays(item.getStartDate(), electricityEndDate));
                                }
                            }
                        });
                double totalUnitsPerPerson = currentConsumption / totalNoOfPerson.get(); //per day


                List<CustomersEbHistory> listEbHistory = listCustomerBedHistory
                        .stream()
                        .map(item -> {
                            double noOfDaysStayed = 0;
                            Date stateDate = new Date();
                            Date eDate = new Date();
                            if (Utils.compareWithTwoDates(item.getStartDate(), electricityStartDate) <= 0 && item.getEndDate() == null) {
                                noOfDaysStayed =  Utils.findNumberOfDays(electricityStartDate, electricityEndDate);
                                stateDate = electricityStartDate;
                                eDate = electricityEndDate;
                            }
                            else if (Utils.compareWithTwoDates(item.getStartDate(), electricityStartDate) <= 0 && item.getEndDate() != null) {
                                if (Utils.compareWithTwoDates(item.getEndDate(), electricityEndDate) <=0) {
                                    //leaving date is before than end date
                                    noOfDaysStayed = Utils.findNumberOfDays(electricityStartDate, item.getEndDate());
                                    stateDate = electricityStartDate;
                                    eDate = item.getEndDate();
                                }
                                else if (Utils.compareWithTwoDates(electricityEndDate, item.getEndDate()) <= 0) {
                                    //leaving date is later than end date
                                    noOfDaysStayed = Utils.findNumberOfDays(electricityStartDate, electricityEndDate);
                                    stateDate = electricityStartDate;
                                    eDate = electricityEndDate;
                                }
                            }
                            else if (Utils.compareWithTwoDates(item.getStartDate(), electricityStartDate) > 0 && item.getEndDate() == null) {
                                noOfDaysStayed = Utils.findNumberOfDays(item.getStartDate(), electricityEndDate);
                                stateDate = item.getStartDate();
                                eDate = electricityEndDate;
                            }
                            else if (Utils.compareWithTwoDates(item.getStartDate(), electricityStartDate) > 0 && item.getEndDate() != null) {
                                if (Utils.compareWithTwoDates(item.getEndDate(), electricityEndDate) <=0) {
                                    noOfDaysStayed =  Utils.findNumberOfDays(item.getStartDate(), item.getEndDate());
                                    stateDate = item.getStartDate();
                                    eDate = item.getEndDate();
                                }
                                else if (Utils.compareWithTwoDates(electricityEndDate, item.getEndDate()) <= 0) {
                                    noOfDaysStayed = Utils.findNumberOfDays(item.getStartDate(), electricityEndDate);
                                    stateDate = item.getStartDate();
                                    eDate = electricityEndDate;
                                }
                            }

                            double noOfUnitsConsumed = noOfDaysStayed * totalUnitsPerPerson;
                            double finalAmount = noOfUnitsConsumed * electricityConfig.getCharge();

                            CustomersEbHistory ebHistory = new CustomersEbHistory();
                            ebHistory.setReadingId(electricityReadings.getId());
                            ebHistory.setCustomerId(item.getCustomerId());
                            ebHistory.setRoomId(item.getRoomId());
                            ebHistory.setFloorId(item.getFloorId());
                            ebHistory.setBedId(item.getBedId());
                            ebHistory.setUnits(noOfUnitsConsumed);
                            ebHistory.setAmount(finalAmount);
                            ebHistory.setStartDate(stateDate);
                            ebHistory.setEndDate(eDate);
                            ebHistory.setCreatedAt(new Date());
                            ebHistory.setCreatedBy(ebHistory.getCreatedBy());

                            return ebHistory;
                        })
                        .toList();

                ebHistoryService.addEbForCustomer(listEbHistory);

            }

        }


    }
}
