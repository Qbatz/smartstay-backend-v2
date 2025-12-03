package com.smartstay.smartstay.responses.beds;

public record BedInitializations(Integer bedId,
                                 Integer roomId,
                                 Integer floorId,
                                 String bedName,
                                 String floorName,
                                 String roomName,
                                 Double rentAmount,
                                 String currentStatus,
                                 boolean shouldShowError,
                                 String errorMessage) {
}
