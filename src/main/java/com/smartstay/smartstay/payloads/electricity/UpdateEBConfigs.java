package com.smartstay.smartstay.payloads.electricity;

import jakarta.validation.constraints.Pattern;

public record UpdateEBConfigs(String frequent,
                              @Pattern(regexp = "ROOM|room|HOSTEL|hostel|FLAT|flat", message = "source must be either 'ROOM' or 'room' or 'HOSTEL' or 'hostel' or 'FLAT' or 'flat'")
                              String typeofReading,
                              Double charge,
                              Boolean shouldIncludeInRent) {
}
