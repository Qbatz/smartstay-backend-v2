package com.smartstay.smartstay.Wrappers.Electricity;

import com.smartstay.smartstay.dto.electricity.ElectricityHistoryBySingleCustomer;
import com.smartstay.smartstay.responses.electricity.CustomersElectricityHistory;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class CustomerHistoryMapper implements Function<ElectricityHistoryBySingleCustomer, CustomersElectricityHistory> {
    @Override
    public CustomersElectricityHistory apply(ElectricityHistoryBySingleCustomer electricityHistoryBySingleCustomer) {
        Double amount = Math.round(electricityHistoryBySingleCustomer.getAmount() * 100.0)/100.0;;
        Double consumption = Math.round(electricityHistoryBySingleCustomer.getConsumption() * 100.0)/100.0;;


        return new CustomersElectricityHistory(electricityHistoryBySingleCustomer.getId(),
                Utils.dateToString(electricityHistoryBySingleCustomer.getStartDate()),
                Utils.dateToString(electricityHistoryBySingleCustomer.getEndDate()),
                electricityHistoryBySingleCustomer.getRoomName(),
                electricityHistoryBySingleCustomer.getFloorName(),
                electricityHistoryBySingleCustomer.getBedName(),
                amount,
                consumption);
    }
}
