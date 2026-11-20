package com.smartstay.smartstay.responses.Reports.invoice;

import java.util.List;

public record InvFloorsRes(Integer floorId,
                           String floorName,
                           List<InvRoomRes> rooms) {
}
