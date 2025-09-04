package com.smartstay.smartstay.responses.customer;

import com.smartstay.smartstay.dto.customer.Deductions;

import java.util.List;

public record HostelInformation(String roomName,
                                Integer roomId,
                                String floorName,
                                Integer floorId,
                                String bedName,
                                Integer bedId,
                                String joiningDate,
                                String currentStatus,
                                Double advanceAmount,
                                Double otherDeductions,
                                Double maintenance,
                                Double monthlyRent,
                                List<Deductions> otherDeductionsBreakup) {
}
