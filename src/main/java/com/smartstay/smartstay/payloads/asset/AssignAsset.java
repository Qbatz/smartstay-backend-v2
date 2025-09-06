package com.smartstay.smartstay.payloads.asset;


public record AssignAsset(
        Integer floorId,
        Integer roomId,
        Integer bedId,
        String assignedAt,
        String hostelId
) {
}
