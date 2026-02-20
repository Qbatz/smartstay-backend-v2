package com.smartstay.smartstay.responses.dashboard;

public record SharingInfo(String shareType,
                          Integer totalBeds,
                          Integer fillBeds,
                          Double occupancyRatio) {
}
