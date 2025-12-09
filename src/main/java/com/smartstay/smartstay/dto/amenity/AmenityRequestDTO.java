package com.smartstay.smartstay.dto.amenity;

public record AmenityRequestDTO(String amenityId,
                                String amenityName,
                                String requestedDate,
                                Double price,
                                String type) {
}
