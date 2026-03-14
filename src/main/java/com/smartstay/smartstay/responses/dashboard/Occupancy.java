package com.smartstay.smartstay.responses.dashboard;

public record Occupancy(Integer occupiedBeds, Integer availableBeds, String occupancyRate, String occupancyRateFromLastMonth) {
}
