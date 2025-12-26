package com.smartstay.smartstay.dto.electricity;

import java.util.Date;

public record BedHistoryByRoomId(Integer roomId, Date startDate, Date endDate) {
}
