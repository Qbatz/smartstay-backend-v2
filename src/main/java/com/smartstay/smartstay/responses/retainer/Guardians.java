package com.smartstay.smartstay.responses.retainer;

public record Guardians(Long guardianId,
                        String guardianName,
                        String country,
                        String mobile,
                        String relationShip) {
}
