package com.smartstay.smartstay.dto.customer;

import java.util.List;

public record WalletInfo(double walletAmount, List<WalletTransactions> transactions) {
}
