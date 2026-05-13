package com.smartstay.smartstay.dto.settlement;

public record AvailableAmountToRedeem(Double availableAdvanceAmountToRedeem,
                                      Double availableBookingAmountToRedeem,
                                      Double totalAvailableAmountToRedeem) {
}
