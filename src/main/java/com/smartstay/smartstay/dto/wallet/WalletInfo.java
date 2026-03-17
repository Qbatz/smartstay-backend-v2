package com.smartstay.smartstay.dto.wallet;

import java.util.List;

public record WalletInfo(Double walletAmount, List<WalletTransactions> transactions) {
}
