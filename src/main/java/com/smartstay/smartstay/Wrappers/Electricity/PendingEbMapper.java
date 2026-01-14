package com.smartstay.smartstay.Wrappers.Electricity;

import com.smartstay.smartstay.dao.CustomersBedHistory;
import com.smartstay.smartstay.dao.CustomersEbHistory;
import com.smartstay.smartstay.dao.ElectricityReadings;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.electricity.PendingEbForSettlement;
import com.smartstay.smartstay.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PendingEbMapper implements Function<CustomersEbHistory, PendingEbForSettlement> {
//    List<CustomersBedHistory> listCustomerBedHistory = new ArrayList<>();
    List<BedDetails> listBeds = new ArrayList<>();

    public PendingEbMapper(List<CustomersBedHistory> listCustomerBedHistory, List<BedDetails> listBeds) {
//        this.listCustomerBedHistory = listCustomerBedHistory;
        this.listBeds = listBeds;
    }

    @Override
    public PendingEbForSettlement apply(CustomersEbHistory customersEbHistory) {
        String bedName = null;
        String floorNmae = null;
        String roomName = null;
        String fromDate = Utils.dateToString(customersEbHistory.getStartDate());
        String endDate = Utils.dateToString(customersEbHistory.getEndDate());
        Double price = null;
//        if (listCustomerBedHistory != null && !listCustomerBedHistory.isEmpty()) {
//            CustomersBedHistory cbh = listCustomerBedHistory
//                    .stream()
//                    .filter(i -> customersEbHistory.getRoomId().equals(i.getRoomId()))
//                    .findFirst()
//                    .orElse(null);
//            if (cbh != null) {
//
//            }
//
//        }
        if (listBeds != null && !listBeds.isEmpty()) {
            BedDetails beds = listBeds
                    .stream()
                    .filter(i -> {
                        System.out.println(i.getRoomId());
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
