package com.smartstay.smartstay.Wrappers.customers;

import com.smartstay.smartstay.dao.CustomersBedHistory;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.responses.customer.RentBreakUp;
import com.smartstay.smartstay.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FinalSettlementMapper implements Function<InvoicesV1, RentBreakUp> {

    List<CustomersBedHistory> listCustomersBedHistory = new ArrayList<>();
    List<BedDetails> listBedDetails = new ArrayList<>();

    public FinalSettlementMapper(List<CustomersBedHistory> listCustomersBedHistory, List<BedDetails> listBedDetails) {
        this.listCustomersBedHistory = listCustomersBedHistory;
        this.listBedDetails = listBedDetails;
    }

    @Override
    public RentBreakUp apply(InvoicesV1 invoicesV1) {
        double rentAmount = invoicesV1.getTotalAmount();
        long noOfDays = Utils.findNumberOfDays(invoicesV1.getInvoiceStartDate(), invoicesV1.getInvoiceEndDate());
        double rentPerDay = invoicesV1.getTotalAmount() / noOfDays;
        String bedName = null;
        String roomName = null;
        String floorName = null;

        List<CustomersBedHistory> findBedFromHistory = listCustomersBedHistory
                .stream()
                .filter(i -> {
                   if (i.getEndDate() == null) {
                       return true;
                   }
                   if (Utils.compareWithTwoDates(i.getStartDate(), invoicesV1.getInvoiceStartDate()) <= 0 && Utils.compareWithTwoDates(i.getEndDate(), invoicesV1.getInvoiceEndDate()) == 0) {
                       return true;
                   }
                   return false;
                })
                .limit(1)
                .toList();
        if (!findBedFromHistory.isEmpty()) {

            BedDetails details = listBedDetails
                    .stream()
                    .filter(i -> i.getBedId().equals(findBedFromHistory.get(0).getBedId()))
                    .findFirst()
                    .orElse(null);
            if (details != null) {
                bedName = details.getBedName();
                floorName = details.getFloorName();
                roomName = details.getRoomName();
            }
        }
        return new RentBreakUp(Utils.dateToString(invoicesV1.getInvoiceStartDate()),
                Utils.dateToString(invoicesV1.getInvoiceEndDate()),
                noOfDays,
                Utils.roundOfDouble(Math.round(rentAmount)),
                Utils.roundOffWithTwoDigit(rentPerDay),
                Utils.roundOfDouble(Math.round(rentAmount)),
                bedName,
                roomName,
                floorName);
    }
}
