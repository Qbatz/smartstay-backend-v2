package com.smartstay.smartstay.Wrappers.reports;

import com.smartstay.smartstay.dao.ElectricityReadings;
import com.smartstay.smartstay.dto.reports.ElectricityForReports;

import java.util.function.Function;

public class ReportsEbMapper implements Function<ElectricityReadings, ElectricityForReports> {
    @Override
    public ElectricityForReports apply(ElectricityReadings electricityReadings) {
        double total = 0.0;
        double consumption = 0.0;
        double charge = 0.0;
        if (electricityReadings.getConsumption() != null) {
            consumption = electricityReadings.getConsumption();
        }
        if (electricityReadings.getCurrentUnitPrice() != null) {
            charge = electricityReadings.getCurrentUnitPrice();
        }

        total = consumption * charge;
        return new ElectricityForReports(consumption, total, 0);
    }
}
