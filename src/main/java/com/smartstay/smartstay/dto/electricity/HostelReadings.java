package com.smartstay.smartstay.dto.electricity;

import java.util.List;

public record HostelReadings(Long id,
                             String startDate,
                             String endDate,
                             String entryDate,
                             Double lastReading,
                             Double consumption) {
}
