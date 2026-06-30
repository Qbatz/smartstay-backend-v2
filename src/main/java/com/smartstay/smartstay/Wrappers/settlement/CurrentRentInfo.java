package com.smartstay.smartstay.Wrappers.settlement;

import com.smartstay.smartstay.dto.settlement.CurrentMonthOtherItems;
import com.smartstay.smartstay.responses.customer.RentBreakUp;

import java.util.List;

public record CurrentRentInfo(String labelText,
                              double currentMonthPaidAmount,
                              double currentMonthPayableAmount,
                              double currentMonthStayDays,
                              double currentMonthRentPayableAmount,
                              double currentMonthOtherItemAmount,
                              List<RentBreakUp> listBreakup,
                              List<CurrentMonthOtherItems> listCurrentMonthOtherItems) {
}
