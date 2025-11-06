package com.smartstay.smartstay.responses.customer;

import java.util.Date;

public record Amenities(String amenityId,
                        String amenityName,
                        Double amenityAmount,
                        String starDate,
                        String endDate) {
}
