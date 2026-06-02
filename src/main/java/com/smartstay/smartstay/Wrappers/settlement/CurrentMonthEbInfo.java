package com.smartstay.smartstay.Wrappers.settlement;

import com.smartstay.smartstay.dto.settlement.EBItems;

import java.util.List;

public record CurrentMonthEbInfo(double currentMonthEbAmount,
                                 List<EBItems> ebItemsList) {
}
