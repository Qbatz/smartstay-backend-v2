package com.smartstay.smartstay.responses.customer;

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
                                Double monthlyRent) {
}
