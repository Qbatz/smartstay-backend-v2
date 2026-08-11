package com.smartstay.smartstay.dto.retainer;

public record RetainerSummary(Double totalRetainerAmount,
                              Double totalBookingAmount,
                              Double totalAdvanceAmount,
                              Double totalEbAmount,
                              Double totalRentAmount,
                              Double otherAmount) {
}
