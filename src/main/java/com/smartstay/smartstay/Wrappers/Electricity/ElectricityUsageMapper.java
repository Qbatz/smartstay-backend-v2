package com.smartstay.smartstay.Wrappers.Electricity;

import com.smartstay.smartstay.responses.electricity.ElectricityUsage;
import com.smartstay.smartstay.responses.rooms.RoomInfoForEB;

import java.util.function.Function;

/**
 *
 * this is for the rooms which is not in eb.
 *
 * like newly created rooms
 *
 */
public class ElectricityUsageMapper implements Function<RoomInfoForEB, ElectricityUsage> {
    @Override
    public ElectricityUsage apply(RoomInfoForEB roomInfoForEB) {

        return new ElectricityUsage(roomInfoForEB.hostelId(),
                0,
                0.0,
                roomInfoForEB.roomId(),
                roomInfoForEB.floorId(),
                roomInfoForEB.roomName(),
                roomInfoForEB.floorName(),
                "N/A",
                0.0,
                0.0,
                0.0,
                0.0,
                roomInfoForEB.noOfTenants());
    }
}
