package com.smartstay.smartstay.eventListeners;

import com.smartstay.smartstay.dao.CustomersEbHistory;
import com.smartstay.smartstay.dao.ElectricityConfig;
import com.smartstay.smartstay.dao.ElectricityReadings;
import com.smartstay.smartstay.dto.booking.BookedCustomerInfoElectricity;
import com.smartstay.smartstay.events.AddEbEvents;
import com.smartstay.smartstay.services.BookingsService;
import com.smartstay.smartstay.services.CustomerEbHistoryService;
import com.smartstay.smartstay.services.ElectricityService;
import com.smartstay.smartstay.services.HostelService;
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

    @Async
    @EventListener
    public void addEbInfoForRoom(AddEbEvents ebEvents) {
        ElectricityConfig electricityConfig = hostelService.getElectricityConfig(ebEvents.getHostelId());
        ElectricityReadings electricityReadings = ebEvents.getElectricityReadings();

        Double currentConsumption = 0.0;

        Date startDate = null;
        if (electricityReadings == null) {
            currentConsumption = ebEvents.getCurrentReading();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(ebEvents.getEntryDate());
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
            currentConsumption = ebEvents.getCurrentReading() - electricityReadings.getCurrentReading();
            Calendar cal = Calendar.getInstance();
            cal.setTime(electricityReadings.getEntryDate());

            startDate = cal.getTime();
        }

        Date endDate = ebEvents.getEntryDate();

        List<BookedCustomerInfoElectricity> listCustomers = bookingService.getAllCheckInCustomers(ebEvents.getRoomId(), startDate, endDate);

        if (!listCustomers.isEmpty()) {
            Date finalStartDate = startDate;
            long personCount = listCustomers
                    .stream()
                    .filter(item -> Utils.compareWithTwoDates(item.getJoiningDate(), finalStartDate) <= 0 && (item.getLeavingDate() == null || Utils.compareWithTwoDates(endDate, item.getLeavingDate()) <= 0)).count();

            if (listCustomers.size() == personCount) {
//                long noOfDaysBetweenStartAndEndDate = Utils.findNumberOfDays(startDate, endDate);
                double finalUnitsPerPerson =  currentConsumption / listCustomers.size();
                double finalAmount = ebEvents.getChargePerUnits() * finalUnitsPerPerson;

                Date finalStartDate1 = startDate;
                List<CustomersEbHistory> listEbHistory = listCustomers
                        .stream()
                        .map(item -> {
                            CustomersEbHistory ebHistory = new CustomersEbHistory();
                            ebHistory.setReadingId(ebEvents.getNewReadingId());
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
                listCustomers
                        .forEach(item -> {
                            if (Utils.compareWithTwoDates(item.getJoiningDate(), finalStartDate2) <= 0 && item.getLeavingDate() == null) {
                                totalNoOfPerson.set(totalNoOfPerson.get() + Utils.findNumberOfDays(finalStartDate2, endDate));
                            }
                            else if (Utils.compareWithTwoDates(item.getJoiningDate(), finalStartDate2) <= 0 && item.getLeavingDate() != null) {
                                if (Utils.compareWithTwoDates(item.getLeavingDate(), endDate) <=0) {
                                    //leaving date is before than end date
                                    totalNoOfPerson.set(totalNoOfPerson.get() + Utils.findNumberOfDays(finalStartDate2, item.getLeavingDate()));
                                }
                                else if (Utils.compareWithTwoDates(endDate, item.getLeavingDate()) <= 0) {
                                    //leaving date is later than end date
                                    totalNoOfPerson.set(totalNoOfPerson.get() + Utils.findNumberOfDays(finalStartDate2, endDate));
                                }
                            }
                            else if (Utils.compareWithTwoDates(item.getJoiningDate(), finalStartDate2) > 0 && item.getLeavingDate() == null) {
                                totalNoOfPerson.set(totalNoOfPerson.get() + Utils.findNumberOfDays(item.getJoiningDate(), endDate));
                            }
                            else if (Utils.compareWithTwoDates(item.getJoiningDate(), finalStartDate2) > 0 && item.getLeavingDate() != null) {
                                if (Utils.compareWithTwoDates(item.getLeavingDate(), endDate) <=0) {
                                    totalNoOfPerson.set(totalNoOfPerson.get() + Utils.findNumberOfDays(item.getJoiningDate(), item.getLeavingDate()));
                                }
                                else if (Utils.compareWithTwoDates(endDate, item.getLeavingDate()) <= 0) {
                                    totalNoOfPerson.set(totalNoOfPerson.get() + Utils.findNumberOfDays(item.getJoiningDate(), endDate));
                                }
                            }
                        });
                double totalUnitsPerPerson = currentConsumption / totalNoOfPerson.get(); //per day


                List<CustomersEbHistory> listEbHistory = listCustomers
                        .stream()
                        .map(item -> {
                            double noOfDaysStayed = 0;
                            Date stateDate = new Date();
                            Date eDate = new Date();
                            if (Utils.compareWithTwoDates(item.getJoiningDate(), finalStartDate2) <= 0 && item.getLeavingDate() == null) {
                                noOfDaysStayed =  Utils.findNumberOfDays(finalStartDate2, endDate);
                                stateDate = finalStartDate2;
                                eDate = endDate;
                            }
                            else if (Utils.compareWithTwoDates(item.getJoiningDate(), finalStartDate2) <= 0 && item.getLeavingDate() != null) {
                                if (Utils.compareWithTwoDates(item.getLeavingDate(), endDate) <=0) {
                                    //leaving date is before than end date
                                    noOfDaysStayed = Utils.findNumberOfDays(finalStartDate2, item.getLeavingDate());
                                    stateDate = finalStartDate2;
                                    eDate = item.getLeavingDate();
                                }
                                else if (Utils.compareWithTwoDates(endDate, item.getLeavingDate()) <= 0) {
                                    //leaving date is later than end date
                                    noOfDaysStayed = Utils.findNumberOfDays(finalStartDate2, endDate);
                                    stateDate = finalStartDate2;
                                    eDate = endDate;
                                }
                            }
                            else if (Utils.compareWithTwoDates(item.getJoiningDate(), finalStartDate2) > 0 && item.getLeavingDate() == null) {
                                noOfDaysStayed = Utils.findNumberOfDays(item.getJoiningDate(), endDate);
                                stateDate = item.getJoiningDate();
                                eDate = endDate;
                            }
                            else if (Utils.compareWithTwoDates(item.getJoiningDate(), finalStartDate2) > 0 && item.getLeavingDate() != null) {
                                if (Utils.compareWithTwoDates(item.getLeavingDate(), endDate) <=0) {
                                    noOfDaysStayed =  Utils.findNumberOfDays(item.getJoiningDate(), item.getLeavingDate());
                                    stateDate = item.getJoiningDate();
                                    eDate = item.getLeavingDate();
                                }
                                else if (Utils.compareWithTwoDates(endDate, item.getLeavingDate()) <= 0) {
                                    noOfDaysStayed = Utils.findNumberOfDays(item.getJoiningDate(), endDate);
                                    stateDate = item.getJoiningDate();
                                    eDate = endDate;
                                }
                            }

                            double noOfUnitsConsumed = noOfDaysStayed * totalUnitsPerPerson;
                            double finalAmount = noOfUnitsConsumed * ebEvents.getChargePerUnits();

                            CustomersEbHistory ebHistory = new CustomersEbHistory();
                            ebHistory.setReadingId(ebEvents.getNewReadingId());
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
