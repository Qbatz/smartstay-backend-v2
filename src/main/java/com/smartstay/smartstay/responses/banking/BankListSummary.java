package com.smartstay.smartstay.responses.banking;

public record BankListSummary(int totalBanks,
                              int noOfActiveBanks,
                              int inactiveBanks,
                              String latestTransactionDate,
                              Double latestTransactionAmount,
                              Double totalTransactions) {
}
