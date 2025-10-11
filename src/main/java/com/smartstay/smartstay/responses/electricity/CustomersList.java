package com.smartstay.smartstay.responses.electricity;

public record CustomersList(String customerId,
                            String firstName,
                            String lastName,
                            String fullName,
                            String initials,
                            String profilePic,
                            Integer floorId,
                            String floorName,
                            Integer roomId,
                            String roomName,
                            Integer bedId,
                            String bedName,
                            Double totalUnits,
                            Double totalAmount,
                            String startDate,
                            String endDate) {
}
