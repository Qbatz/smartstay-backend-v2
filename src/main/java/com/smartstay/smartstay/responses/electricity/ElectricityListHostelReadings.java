package com.smartstay.smartstay.responses.electricity;

import com.smartstay.smartstay.dto.electricity.HostelReadings;

import java.util.List;

public record ElectricityListHostelReadings(String hostelId,
                                            Double lastReading,
                                            boolean isHostelBased,
                                            List<ElectricityUsage> listReadings,
                                            List<HostelReadings> hostelReadings) {
}
