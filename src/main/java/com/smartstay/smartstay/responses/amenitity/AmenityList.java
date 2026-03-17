package com.smartstay.smartstay.responses.amenitity;

import java.util.List;

public record AmenityList(String hostelId, List<AmenityInfoProjection> amenities) {
}
