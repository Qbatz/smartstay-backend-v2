package com.smartstay.smartstay.responses.banking;

import java.util.List;

public record BankOverviewResponse(
        OverviewFilterOptions filterOptions,
        Double currentBalance,
        Double openingBalance,
        Double invoiceAmount,
        Double assetsAmount,
        Double bookingRefundAmount,
        Double depositAmount,
        Double selfTransferAmount,
        Double rentRefundAmount,
        Double expenseAmount,
        List<MonthOverview> monthData
) {
}
