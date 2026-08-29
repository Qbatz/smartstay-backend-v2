package com.smartstay.smartstay.payloads.electricity;

public record ResetElectricity(Integer roomId,
                               String resetOn,
                               Double startReading,
                               String resetReason) {
}
