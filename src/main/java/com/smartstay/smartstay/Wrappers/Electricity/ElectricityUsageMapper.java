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

    private String startDate = null;
    private String endDate = null;

    public ElectricityUsageMapper(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public ElectricityUsage apply(RoomInfoForEB roomInfoForEB) {
        String[] sDateArr = startDate.split("-");
        String[] eDateArr = endDate.split("-");

        String sDate = String.join("/", sDateArr);
        String eDate =  String.join("/", eDateArr);

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
                roomInfoForEB.noOfTenants().intValue(),
                sDate,
                eDate);
    }
}
