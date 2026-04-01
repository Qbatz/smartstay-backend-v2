package com.smartstay.smartstay.Wrappers.customers;

import com.smartstay.smartstay.dao.CustomersBedHistory;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.responses.customer.RentBreakUp;
import com.smartstay.smartstay.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class BedHistoryBreakupMapper implements Function<CustomersBedHistory, RentBreakUp> {
    private List<BedDetails> listDetails = new ArrayList<>();
    private Date leavingDate = null;
    private BillingDates billingDates;
    public BedHistoryBreakupMapper(List<BedDetails> listDetails, Date leavingDate, BillingDates billingDates) {
        this.listDetails = listDetails;
        this.leavingDate = leavingDate;
        this.billingDates = billingDates;
    }

    @Override
    public RentBreakUp apply(CustomersBedHistory customersBedHistory) {
        String startDate = null;
        String endDate = null;
        long noOfDays = 0;
        double rentPerDay = 0;
        double rent = 0;
        double totalRent = 0;
        String bedName = null;
        String roomName = null;
        String floorName = null;

        Date dStartDate = null;
        Date dEndDate = null;

        if (listDetails != null) {
            BedDetails bedDetails = listDetails.stream()
                    .filter(i -> i.getBedId().equals(customersBedHistory.getBedId()))
                    .findFirst()
                    .orElse(null);
            if (bedDetails != null) {
                bedName = bedDetails.getBedName();
                roomName = bedDetails.getRoomName();
                floorName = bedDetails.getFloorName();
            }
        }
        if (Utils.compareWithTwoDates(customersBedHistory.getStartDate(), billingDates.currentBillStartDate()) < 0) {
            startDate = Utils.dateToString(billingDates.currentBillStartDate());
            dStartDate = billingDates.currentBillStartDate();
        }
        else {
            startDate = Utils.dateToString(customersBedHistory.getStartDate());
            dStartDate = customersBedHistory.getStartDate();
        }
        if (customersBedHistory.getEndDate() == null) {
            dEndDate = leavingDate;
            endDate = Utils.dateToString(leavingDate);
        }
        else {
            dEndDate = customersBedHistory.getEndDate();
            endDate = Utils.dateToString(customersBedHistory.getEndDate());
        }

        noOfDays = Utils.findNumberOfDays(dStartDate, dEndDate);
        long noOfDaysInCurrentMonth = Utils.findNoOfDaysInCurrentMonth(leavingDate);
        rentPerDay = customersBedHistory.getRentAmount() / noOfDaysInCurrentMonth;
        totalRent = rentPerDay * noOfDays;
        rent = rentPerDay * noOfDays;

        return new RentBreakUp(startDate,
                endDate,
                noOfDays,
                Utils.roundOffWithTwoDigit(rentPerDay),
                rent,
                Utils.roundOffWithTwoDigit(totalRent),
                bedName,
                roomName,
                floorName);
    }
}
