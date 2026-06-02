package com.smartstay.smartstay.responses.settlement;

public record ElectricityInfoItems(String roomName,
                                   String floorName,
                                   String bedName,
                                   Double amount,
                                   Double units) {
}
