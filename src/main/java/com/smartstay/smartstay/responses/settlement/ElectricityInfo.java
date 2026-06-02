package com.smartstay.smartstay.responses.settlement;

import java.util.List;

public record ElectricityInfo(Double totalAmount,
                              Double totalUnits,
                              List<ElectricityInfoItems> breakupList) {
}
