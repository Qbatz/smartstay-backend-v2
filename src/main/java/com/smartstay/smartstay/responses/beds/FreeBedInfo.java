package com.smartstay.smartstay.responses.beds;

public record FreeBedInfo(Integer bedId,
                          Integer floorId,
                          Integer roomId,
                          String bedName,
                          String bedStatus,
                          String expectedJoiningDate,
                          String relievingDate,
                          String roomName,
                          String floorName) {
}
