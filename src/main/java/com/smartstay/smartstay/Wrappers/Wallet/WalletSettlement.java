package com.smartstay.smartstay.Wrappers.Wallet;

import com.smartstay.smartstay.dao.CustomerWalletHistory;
import com.smartstay.smartstay.dto.wallet.WalletTransactions;
import com.smartstay.smartstay.ennum.WalletSource;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class WalletSettlement implements Function<CustomerWalletHistory, WalletTransactions> {
    @Override
    public WalletTransactions apply(CustomerWalletHistory customerWalletHistory) {
        String fromDate = null;
        String toDate = null;
        String source = null;

        if (customerWalletHistory.getBillStartDate() != null) {
            fromDate = Utils.dateToString(customerWalletHistory.getBillStartDate());
        }
        if (customerWalletHistory.getBillEndDate() != null) {
            toDate = Utils.dateToString(customerWalletHistory.getBillEndDate());
        }

        if (customerWalletHistory.getSourceType().equalsIgnoreCase(WalletSource.ELECTRICITY.name())) {
            source = "Electricity";
        }
        else if (customerWalletHistory.getSourceType().equalsIgnoreCase(WalletSource.CHANGE_BED.name())) {
            source = "Change Bed";
        }
        else if (customerWalletHistory.getSourceType().equalsIgnoreCase(WalletSource.AMENITY.name())) {
            source = "Amenity";
        }

        return new WalletTransactions(Utils.roundOffWithTwoDigit(customerWalletHistory.getAmount()),
                source,
                String.valueOf(customerWalletHistory.getSourceId()),
                fromDate,
                toDate);
    }
}
