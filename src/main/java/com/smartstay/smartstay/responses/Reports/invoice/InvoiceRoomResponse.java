package com.smartstay.smartstay.responses.Reports.invoice;

import java.util.List;

public record InvoiceRoomResponse(Integer roomId,
                                  String roomName,
                                  Integer floorId,
                                  String floorName,
                                  List<InvoiceBedResponse> beds) {
}
