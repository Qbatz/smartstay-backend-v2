package com.smartstay.smartstay.dto.electricity;

public record PendingEbForSettlement(Integer roomId,
                                     String bedName,
                                     String roomName,
                                     String floorName,
                                     Double unit,
                                     Double price,
                                     String fromDate,
                                     String toDate) {
}
