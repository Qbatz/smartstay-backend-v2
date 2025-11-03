package com.smartstay.smartstay.dto.beds;

public record BedInformations(Integer bedId,
                              String bedName,
                              Integer floorId,
                              String floorName,
                              Integer roomId,
                              String roomName) {
}
