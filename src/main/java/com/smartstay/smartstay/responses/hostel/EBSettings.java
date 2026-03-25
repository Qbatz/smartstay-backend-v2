package com.smartstay.smartstay.responses.hostel;

public record EBSettings(String hostelId,
                         Double chargerPerUnit,
                         String typeOfReading,
                         boolean shouldIncludeInRent,
                         Double charge) {
}
