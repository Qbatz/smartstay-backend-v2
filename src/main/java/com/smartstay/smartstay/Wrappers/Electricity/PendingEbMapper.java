package com.smartstay.smartstay.Wrappers.Electricity;

import com.smartstay.smartstay.dao.CustomersBedHistory;
import com.smartstay.smartstay.dao.CustomersEbHistory;
import com.smartstay.smartstay.dao.ElectricityReadings;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.electricity.PendingEbForSettlement;
import com.smartstay.smartstay.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class PendingEbMapper implements Function<CustomersEbHistory, PendingEbForSettlement> {
//    List<CustomersBedHistory> listCustomerBedHistory = new ArrayList<>();
    List<BedDetails> listBeds = new ArrayList<>();
    List<CustomersBedHistory> customersBedHistories = new ArrayList<>();
    Date leavingDate = null;

    public PendingEbMapper(List<CustomersBedHistory> listCustomerBedHistory, List<BedDetails> listBeds, Date leavingDate) {
        this.customersBedHistories = listCustomerBedHistory;
        this.listBeds = listBeds;
        this.leavingDate = leavingDate;
    }

    @Override
    public PendingEbForSettlement apply(CustomersEbHistory customersEbHistory) {
        String bedName = null;
        String floorNmae = null;
        String roomName = null;
        String fromDate = Utils.dateToString(customersEbHistory.getStartDate());
        String endDate = Utils.dateToString(customersEbHistory.getEndDate());
        Double price = null;


        if (customersBedHistories != null && !customersBedHistories.isEmpty()) {
            CustomersBedHistory cbh = customersBedHistories
                    .stream()
                    .filter(i -> customersEbHistory.getRoomId().equals(i.getRoomId()))
                    .findFirst()
                    .orElse(null);
            if (cbh != null) {
                if (leavingDate != null) {
                    if (Utils.compareWithTwoDates(leavingDate, customersEbHistory.getEndDate()) < 0) {
                        endDate = Utils.dateToString(leavingDate);
                    }
                }
            }

        }
        if (listBeds != null && !listBeds.isEmpty()) {
            BedDetails beds = listBeds
                    .stream()
                    .filter(i -> {
                        return customersEbHistory.getRoomId().equals(i.getRoomId());
                    })
                    .findFirst()
                    .orElse(null);
            if (beds != null) {
                bedName = beds.getBedName();
                floorNmae = beds.getFloorName();
                roomName = beds.getRoomName();
            }
        }
        return new PendingEbForSettlement(customersEbHistory.getRoomId(),
                bedName,
                roomName,
                floorNmae,
                Utils.roundOffWithTwoDigit(customersEbHistory.getUnits()),
                Utils.roundOffWithTwoDigit(customersEbHistory.getAmount()),
                fromDate,
                endDate);
    }
}
