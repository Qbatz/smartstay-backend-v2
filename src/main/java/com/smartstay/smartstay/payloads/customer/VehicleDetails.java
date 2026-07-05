package com.smartstay.smartstay.payloads.customer;

/**
 * Optional vehicle details captured with a customer draft. All fields are non-mandatory; used both as
 * a request sub-object (Save/Update Draft) and in the draft response.
 */
public record VehicleDetails(
        String vehicleType,
        String vehicleNumber,
        Boolean isParkingSpaceRequired
) {
}
