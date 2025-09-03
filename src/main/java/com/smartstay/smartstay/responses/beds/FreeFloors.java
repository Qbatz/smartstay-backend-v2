package com.smartstay.smartstay.responses.beds;

import java.util.List;

public record FreeFloors(List<FreeRooms> rooms, Integer floorId) {
}

