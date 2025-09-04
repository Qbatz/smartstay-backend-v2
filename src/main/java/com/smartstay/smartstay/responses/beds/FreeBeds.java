package com.smartstay.smartstay.responses.beds;

import java.util.List;

public record FreeBeds(Integer bedId,
                       Integer roomId,
                       Integer floorId,
                       String bedName,
                       Double rentAmount,
                       String bedStatus,
                       String joiningDate,
                       String relievingDate,
                       String roomName,
                       String floorName,
                       boolean showWarning,
                       String warningMessage,
                       long noOfDaysAvailable) {
}

