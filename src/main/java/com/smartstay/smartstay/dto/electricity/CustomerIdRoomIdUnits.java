package com.smartstay.smartstay.dto.electricity;

import java.util.Date;

public record CustomerIdRoomIdUnits(String customerId,
                                    Integer roomId,
                                    Integer bedId,
                                    Double units,
                                    Date startDate,
                                    Date endDate) {
}
