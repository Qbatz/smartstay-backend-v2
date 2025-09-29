package com.smartstay.smartstay.payloads.electricity;

public record UpdateEBConfigs(Boolean isRoomBased,
                              Boolean isHostelBased,
                              Boolean isProRate,
                              Integer calculationStartingDate,
                              String frequent,
                              Boolean shouldIncludeInRent) {
}
