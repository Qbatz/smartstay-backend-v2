package com.smartstay.smartstay.responses.electricity;

import com.smartstay.smartstay.responses.rooms.RoomInfoForEB;

import java.util.List;

public record ElectricityList(String hostelId, Double lastReading, boolean isHostelBased, List<ElectricityUsage> listReadings) {
}
