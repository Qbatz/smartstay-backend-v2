package com.smartstay.smartstay.dto.electricity;

import java.util.Date;

public record CustomerBedsList(String customerId,
                               Integer bedId,
                               Integer roomId,
                               Date startDate,
                               Date endDate) {
}
