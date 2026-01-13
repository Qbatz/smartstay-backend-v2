package com.smartstay.smartstay.dto.electricity;

public record MissedEbRooms(Integer roomId,
                            String roomName,
                            String bedName,
                            String floorName,
                            String fromDate,
                            String toDate) {
}
