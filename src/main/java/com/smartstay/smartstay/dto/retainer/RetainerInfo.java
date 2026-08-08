package com.smartstay.smartstay.dto.retainer;

import java.util.List;

public record RetainerInfo(int totalRetainerInvoices,
                           Double totalAvailableAmount,
                           Double totalRetainerValue,
                           List<RetainerItems> listRetainerItems) {
}
