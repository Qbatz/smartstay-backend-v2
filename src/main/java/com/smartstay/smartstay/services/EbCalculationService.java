package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.CustomersBedHistory;
import com.smartstay.smartstay.dao.CustomersEbHistory;
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

    public List<PendingEbForSettlement> calculateEbAmountAndUnit(String hostelId, String customerId, List<ElectricityReadings> pendingHistoryAmount, List<BedDetails> roomInfo, Date leavingDate) {
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
                                    if (leavingDate != null) {
                                        if (Utils.compareWithTwoDates(leavingDate, i.getBillEndDate()) <= 0) {
                                            endDate.set(leavingDate);
                                        }
                                        else {
                                            endDate.set(i.getBillEndDate());
                                        }
                                    }
                                    else {
                                        endDate.set(i.getBillEndDate());
                                    }
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
                            bedDetails.get().getBedName(),
                            bedDetails.get().getRoomName(),
                            bedDetails.get().getFloorName(),
                            Utils.roundOffWithTwoDigit(totalUnitsPerPersion),
                            Utils.roundOfDouble(price),
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

    public List<CustomersEbHistory> calculateEbAmountAndUnitForAll(String hostelId, List<ElectricityReadings> pendingHistoryAmount) {
        ElectricityConfig electricityConfig = hostelService.getElectricityConfig(hostelId);
        List<CustomersEbHistory> customerHistory = new ArrayList<>();
        pendingHistoryAmount.forEach(item -> {
            List<CustomersEbHistory> customersEbHistories = formCustomerEbHistory(item, electricityConfig);
            if (customersEbHistories != null) {
               customerHistory.addAll(customersEbHistories);
            }
        });

        return customerHistory;
    }

    private List<CustomersEbHistory> formCustomerEbHistory(ElectricityReadings electricityReadings, ElectricityConfig electricityConfig) {
        Date electricityStartDate = electricityReadings.getBillStartDate();
        Date electricityEndDate = electricityReadings.getBillEndDate();
        List<CustomersBedHistory> listCustomerBedHistory = customersBedHistoryService.getCustomersByRoomIdAndDates(electricityReadings.getRoomId(), electricityReadings.getBillStartDate(), electricityReadings.getBillEndDate());
        if (!listCustomerBedHistory.isEmpty()) {
            if (!listCustomerBedHistory.isEmpty()) {
                long personCount = listCustomerBedHistory
                        .stream()
                        .filter(item -> Utils.compareWithTwoDates(item.getStartDate(), electricityReadings.getBillStartDate()) <= 0 && (item.getEndDate() == null || Utils.compareWithTwoDates(electricityReadings.getEntryDate(), item.getEndDate()) <= 0)).count();

                if (listCustomerBedHistory.size() == personCount) {
//                long noOfDaysBetweenStartAndEndDate = Utils.findNumberOfDays(startDate, endDate);
                    double finalUnitsPerPerson =  electricityReadings.getConsumption() / listCustomerBedHistory.size();
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

                    return listEbHistory;
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
                    double totalUnitsPerPerson = electricityReadings.getConsumption() / totalNoOfPerson.get(); //per day


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

                    return listEbHistory;

                }

            }
        }
        return null;
    }

}
