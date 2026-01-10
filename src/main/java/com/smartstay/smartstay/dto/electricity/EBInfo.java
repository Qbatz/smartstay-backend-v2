package com.smartstay.smartstay.dto.electricity;

//this is for generating final settlement
public record EBInfo(Double lastReading,
                     Double unitPrice,
                     String lastEntryDate,
                     String typeOfReading,
                     boolean isHostelReading,
                     boolean canAddEb) {
}
