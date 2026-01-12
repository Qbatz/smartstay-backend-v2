package com.smartstay.smartstay.dto.electricity;

import java.util.List;

//this is for generating final settlement
public record EBInfo(Double lastReading,
                     Double unitPrice,
                     String lastEntryDate,
                     String typeOfReading,
                     boolean isHostelReading,
                     boolean canAddEb,
                     List<MissedEbRooms> missedEb,
                     List<PendingEbForSettlement> pendingEb) {
}
