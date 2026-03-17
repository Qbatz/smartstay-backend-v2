package com.smartstay.smartstay.Wrappers.Wallet;

import com.smartstay.smartstay.dao.CustomerWalletHistory;
import com.smartstay.smartstay.dto.customer.WalletTransactions;
import com.smartstay.smartstay.ennum.WalletBillingStatus;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class WalltetTransactionMapper implements Function<CustomerWalletHistory, WalletTransactions> {
    @Override
    public WalletTransactions apply(CustomerWalletHistory customerWalletHistory) {
        String transactionDate = null;
        boolean isInvoiced = false;
        String billStartDate = null;
        String billEndDate = null;
        if (customerWalletHistory.getTransactionDate() != null) {
            transactionDate = Utils.dateToString(customerWalletHistory.getTransactionDate());
        }
        if (customerWalletHistory.getBillingStatus().equalsIgnoreCase(WalletBillingStatus.INVOICE_GENERATED.name())) {
            isInvoiced = true;
        }
        if (customerWalletHistory.getBillStartDate() != null) {
            billStartDate = Utils.dateToString(customerWalletHistory.getBillStartDate());
        }
        if (customerWalletHistory.getBillEndDate() != null) {
            billEndDate = Utils.dateToString(customerWalletHistory.getBillEndDate());
        }
        return new WalletTransactions(transactionDate,
                Utils.roundOfDouble(customerWalletHistory.getAmount()),
                customerWalletHistory.getSourceType(),
                customerWalletHistory.getBillingStatus(),
                billStartDate,
                billEndDate,
                isInvoiced);
    }
}
