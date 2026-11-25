package com.smartstay.smartstay.responses.Reports.invoice;

import java.util.List;

public record InvoiceFloorsResponse(Integer floorId,
                                    String floorName,
                                    List<InvoiceRoomResponse> rooms) {
}
