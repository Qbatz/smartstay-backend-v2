package com.smartstay.smartstay.responses.hostel;

public record EBSettings(String hostelId,
                         Double chargerPerUnit,
                         boolean isHostelBased,
                         boolean isRoomBased,
                         boolean isProRate) {
}
