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
        String startDateStr = "NA";
        String endDateStr = "NA";
        if (startDate != null) {
            String[] sDateArr = startDate.split("-");
            StringBuilder sDate = new StringBuilder();

            for (int i = sDateArr.length; i>0; i-- ) {
                sDate.append(sDateArr[i - 1]);
                if ((i - 1) != 0) {
                    sDate.append("/");
                }
            }
            startDateStr = sDate.toString();
        }

        if (endDate != null) {
            String[] eDateArr = endDate.split("-");
            StringBuilder eDate = new StringBuilder();

            for (int i = eDateArr.length; i>0; i-- ) {
                eDate.append(eDateArr[i - 1]);
                if ((i - 1) != 0) {
                    eDate.append("/");
                }

            }
            endDateStr = eDate.toString();
        }

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
                startDateStr,
                endDateStr);
    }
}
