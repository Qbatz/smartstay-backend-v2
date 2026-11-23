package com.smartstay.smartstay.responses.Reports.invoice;

import java.util.List;

public record InvRoomRes(Integer roomId,
                         String roomName,
                         Integer floorId,
                         String floorName,
                         List<InvBedRes> beds) {
}
