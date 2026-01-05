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

        Double currentConsumption = 0.0;

        Date startDate = null;
        if (electricityReadings == null) {
            currentConsumption = electricityReadings.getCurrentReading();
            Calendar calendar = Calendar.getInstance();
            int startDay = 1;
            if (electricityConfig == null) {
                calendar.set(Calendar.DAY_OF_MONTH, startDay);
            }
            else {
                calendar.set(Calendar.DAY_OF_MONTH, electricityConfig.getBillDate());
            }

            startDate = calendar.getTime();

        }
        else {
            currentConsumption =electricityReadings.getConsumption();
            Calendar cal = Calendar.getInstance();
            cal.setTime(electricityReadings.getEntryDate());
//            cal.add(Calendar.DAY_OF_MONTH, 1);

            startDate = electricityReadings.getBillStartDate();
        }

        Date endDate = electricityReadings.getEntryDate();

        List<CustomersBedHistory> listCustomerBedHistory = customerBedHistory.getCustomersByBedIdAndDates(ebEvents.getRoomId(), startDate, endDate);
        if (!listCustomerBedHistory.isEmpty()) {
            Date finalStartDate = startDate;
            long personCount = listCustomerBedHistory
                    .stream()
                    .filter(item -> Utils.compareWithTwoDates(item.getStartDate(), finalStartDate) <= 0 && (item.getEndDate() == null || Utils.compareWithTwoDates(endDate, item.getEndDate()) <= 0)).count();

            if (listCustomerBedHistory.size() == personCount) {
//                long noOfDaysBetweenStartAndEndDate = Utils.findNumberOfDays(startDate, endDate);
                double finalUnitsPerPerson =  currentConsumption / listCustomerBedHistory.size();
                double finalAmount = electricityConfig.getCharge() * finalUnitsPerPerson;

                Date finalStartDate1 = startDate;
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
                            ebHistory.setStartDate(finalStartDate1);
                            ebHistory.setEndDate(endDate);
                            ebHistory.setCreatedAt(new Date());
                            ebHistory.setCreatedBy(ebHistory.getCreatedBy());

                            return ebHistory;
                        })
                        .toList();

                ebHistoryService.addEbForCustomer(listEbHistory);
            }
            else {
                Date finalStartDate2 = startDate;
                AtomicLong totalNoOfPerson = new AtomicLong();
                listCustomerBedHistory
                        .forEach(item -> {
                            if (Utils.compareWithTwoDates(item.getStartDate(), finalStartDate2) <= 0 && item.getEndDate() == null) {
                                totalNoOfPerson.set(totalNoOfPerson.get() + Utils.findNumberOfDays(finalStartDate2, endDate));
                            }
                            else if (Utils.compareWithTwoDates(item.getStartDate(), finalStartDate2) <= 0 && item.getEndDate() != null) {
                                if (Utils.compareWithTwoDates(item.getEndDate(), endDate) <=0) {
                                    //leaving date is before than end date
                                    totalNoOfPerson.set(totalNoOfPerson.get() + Utils.findNumberOfDays(finalStartDate2, item.getEndDate()));
                                }
                                else if (Utils.compareWithTwoDates(endDate, item.getEndDate()) <= 0) {
                                    //leaving date is later than end date
                                    totalNoOfPerson.set(totalNoOfPerson.get() + Utils.findNumberOfDays(finalStartDate2, endDate));
                                }
                            }
                            else if (Utils.compareWithTwoDates(item.getStartDate(), finalStartDate2) > 0 && item.getEndDate() == null) {
                                totalNoOfPerson.set(totalNoOfPerson.get() + Utils.findNumberOfDays(item.getStartDate(), endDate));
                            }
                            else if (Utils.compareWithTwoDates(item.getStartDate(), finalStartDate2) > 0 && item.getEndDate() != null) {
                                if (Utils.compareWithTwoDates(item.getEndDate(), endDate) <=0) {
                                    totalNoOfPerson.set(totalNoOfPerson.get() + Utils.findNumberOfDays(item.getStartDate(), item.getEndDate()));
                                }
                                else if (Utils.compareWithTwoDates(endDate, item.getEndDate()) <= 0) {
                                    totalNoOfPerson.set(totalNoOfPerson.get() + Utils.findNumberOfDays(item.getStartDate(), endDate));
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
                            if (Utils.compareWithTwoDates(item.getStartDate(), finalStartDate2) <= 0 && item.getEndDate() == null) {
                                noOfDaysStayed =  Utils.findNumberOfDays(finalStartDate2, endDate);
                                stateDate = finalStartDate2;
                                eDate = endDate;
                            }
                            else if (Utils.compareWithTwoDates(item.getStartDate(), finalStartDate2) <= 0 && item.getEndDate() != null) {
                                if (Utils.compareWithTwoDates(item.getEndDate(), endDate) <=0) {
                                    //leaving date is before than end date
                                    noOfDaysStayed = Utils.findNumberOfDays(finalStartDate2, item.getEndDate());
                                    stateDate = finalStartDate2;
                                    eDate = item.getEndDate();
                                }
                                else if (Utils.compareWithTwoDates(endDate, item.getEndDate()) <= 0) {
                                    //leaving date is later than end date
                                    noOfDaysStayed = Utils.findNumberOfDays(finalStartDate2, endDate);
                                    stateDate = finalStartDate2;
                                    eDate = endDate;
                                }
                            }
                            else if (Utils.compareWithTwoDates(item.getStartDate(), finalStartDate2) > 0 && item.getEndDate() == null) {
                                noOfDaysStayed = Utils.findNumberOfDays(item.getStartDate(), endDate);
                                stateDate = item.getStartDate();
                                eDate = endDate;
                            }
                            else if (Utils.compareWithTwoDates(item.getStartDate(), finalStartDate2) > 0 && item.getEndDate() != null) {
                                if (Utils.compareWithTwoDates(item.getEndDate(), endDate) <=0) {
                                    noOfDaysStayed =  Utils.findNumberOfDays(item.getStartDate(), item.getEndDate());
                                    stateDate = item.getStartDate();
                                    eDate = item.getEndDate();
                                }
                                else if (Utils.compareWithTwoDates(endDate, item.getEndDate()) <= 0) {
                                    noOfDaysStayed = Utils.findNumberOfDays(item.getStartDate(), endDate);
                                    stateDate = item.getStartDate();
                                    eDate = endDate;
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
