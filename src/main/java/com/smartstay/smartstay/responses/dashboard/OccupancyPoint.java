package com.smartstay.smartstay.responses.dashboard;

public record OccupancyPoint(String date, Integer booked, Integer occupied, Integer vacant) {
}
