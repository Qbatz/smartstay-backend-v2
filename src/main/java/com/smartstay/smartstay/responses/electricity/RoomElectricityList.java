package com.smartstay.smartstay.responses.electricity;

public record RoomElectricityList(int ebId,
                                  Double unitPrice,
                                  String hostelId,
                                  Integer roomId,
                                  String startDate,
                                  String endDate,
                                  Integer consumption,
                                  Integer reading) {
}
