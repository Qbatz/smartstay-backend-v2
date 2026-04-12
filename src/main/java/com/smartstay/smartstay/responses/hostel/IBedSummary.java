package com.smartstay.smartstay.responses.hostel;

public interface IBedSummary {
    // These match the SQL aliases exactly
    Long getTotalBeds();
    Long getAvailableBeds();
    Long getOccupiedBeds();

    // Default methods to handle cases where the entire object or fields are null
    default long getTotal() {
        return getTotalBeds() == null ? 0L : getTotalBeds();
    }

    default long getAvailable() {
        return getAvailableBeds() == null ? 0L : getAvailableBeds();
    }

    default long getOccupied() {
        return getOccupiedBeds() == null ? 0L : getOccupiedBeds();
    }
}