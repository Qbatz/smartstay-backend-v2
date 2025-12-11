package com.smartstay.smartstay.responses.electricity;

public record RoomElectricityList(int ebId,
                                  Double unitPrice,
                                  String hostelId,
                                  Integer roomId,
                                  String startDate,
                                  String endDate,
                                  Double consumption,
                                  Double reading,
                                  String entryDate,
                                  Double amount) {
}
