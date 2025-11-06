package com.smartstay.smartstay.dto.dashboard;

public record BedsStatus(Integer totalBeds,
                         Integer freeBeds,
                         Integer occupiedBeds,
                         Integer bookedBeds) {
}
