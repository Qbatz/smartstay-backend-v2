package com.smartstay.smartstay.responses.banking;

public record MonthOverview(
        String month,
        Double currentBalance,
        Double openingBalance,
        Double invoiceAmount,
        Double assetsAmount,
        Double bookingRefundAmount,
        Double depositAmount,
        Double selfTransferAmount,
        Double rentRefundAmount,
        Double expenseAmount
) {
}
