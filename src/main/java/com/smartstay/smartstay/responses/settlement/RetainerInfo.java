package com.smartstay.smartstay.responses.settlement;

import java.util.List;

public record RetainerInfo(int totalRetainerApplied,
                           Double totalRetainerAmount,
                           List<RetainerItems> retainerItems) {
}
