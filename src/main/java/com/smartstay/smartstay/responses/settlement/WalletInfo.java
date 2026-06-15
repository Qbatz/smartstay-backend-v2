package com.smartstay.smartstay.responses.settlement;

import java.util.List;

public record WalletInfo(Integer noOfItems,
                         Double totalWalletAmount,
                         List<WalletItems> walletItems) {
}
