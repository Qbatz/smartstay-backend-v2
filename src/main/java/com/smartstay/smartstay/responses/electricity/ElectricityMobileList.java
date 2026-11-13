package com.smartstay.smartstay.responses.electricity;

import java.util.List;

public record ElectricityMobileList(String hostelId,
                                    Double lastReading,
                                    boolean isHostelBased,
                                    int currentPage,
                                    int totalPages,
                                    int itemPerPage,
                                    List<ElectricityUsage> listReadings) {
}
