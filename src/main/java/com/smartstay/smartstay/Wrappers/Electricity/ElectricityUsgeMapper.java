package com.smartstay.smartstay.Wrappers.Electricity;

import com.smartstay.smartstay.dao.ElectricityReadings;
import com.smartstay.smartstay.dto.room.RoomInfo;
import com.smartstay.smartstay.responses.electricity.ElectricityUsage;
import com.smartstay.smartstay.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class ElectricityUsgeMapper implements Function<ElectricityReadings, ElectricityUsage> {

    HashMap<Integer, Integer> occupantCounts = null;
    List<RoomInfo> listRoomInfo = null;

    public ElectricityUsgeMapper(HashMap<Integer, Integer> occupantCounts, List<RoomInfo> roomInfos) {
        this.occupantCounts = occupantCounts;
        this.listRoomInfo = roomInfos;
    }

    @Override
    public ElectricityUsage apply(ElectricityReadings electricityReadings) {
        Double totalPrice = 0.0;
        Integer noOfTenants = 0;
        String roomName = null;
        String floorName = null;
        if (occupantCounts != null) {
            noOfTenants = occupantCounts.get(electricityReadings.getRoomId());
            if (noOfTenants == null) {
                noOfTenants = 0;
            }
        }
        if (!electricityReadings.isFirstEntry()) {
            totalPrice = electricityReadings.getCurrentUnitPrice() * electricityReadings.getConsumption();
        }
        if (listRoomInfo != null) {
            RoomInfo roomInfo = listRoomInfo
                    .stream()
                    .filter(i -> i.getRoomId().equals(electricityReadings.getRoomId()))
                    .findFirst()
                    .orElse(null);

            if (roomInfo != null) {
                roomName = roomInfo.getRoomName();
                floorName = roomInfo.getFloorName();
            }
        }

        return new ElectricityUsage(electricityReadings.getHostelId(),
                electricityReadings.getId(),
                Utils.roundOffWithTwoDigit(electricityReadings.getConsumption()),
                electricityReadings.getRoomId(),
                electricityReadings.getFloorId(),
                roomName,
                floorName,
                Utils.dateToString(electricityReadings.getEntryDate()),
                electricityReadings.getCurrentUnitPrice(),
                Utils.roundOffWithTwoDigit(electricityReadings.getPreviousReading()),
                Utils.roundOffWithTwoDigit(electricityReadings.getCurrentReading()),
                Utils.roundOffWithTwoDigit(totalPrice),
                noOfTenants,
                Utils.dateToString(electricityReadings.getBillStartDate()),
                Utils.dateToString(electricityReadings.getBillEndDate()));
    }
}
