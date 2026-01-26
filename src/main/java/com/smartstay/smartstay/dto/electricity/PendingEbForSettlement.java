package com.smartstay.smartstay.dto.electricity;

public record PendingEbForSettlement(Integer roomId,
                                     String bedName,
                                     String roomName,
                                     String floorName,
                                     Double units,
                                     Double amount,
                                     String fromDate,
                                     String toDate) {
}
