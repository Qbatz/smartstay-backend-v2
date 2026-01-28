package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.Wallet.WalltetTransactionMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.CustomerWalletHistory;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.CustomersEbHistory;
import com.smartstay.smartstay.dto.customer.WalletTransactions;
import com.smartstay.smartstay.ennum.ElectricityBillStatus;
import com.smartstay.smartstay.ennum.SourceType;
import com.smartstay.smartstay.ennum.WalletBillingStatus;
import com.smartstay.smartstay.ennum.WalletTransactionType;
import com.smartstay.smartstay.repositories.CustomerWalletHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CustomerWalletHistoryService {
    @Autowired
    private CustomerWalletHistoryRepository walletHistoryRepository;
    @Autowired
    private Authentication authentication;

    public CustomerWalletHistory formWalletHistory(String customerId, CustomersEbHistory history, String createdBy) {
        CustomerWalletHistory walletHistory = new CustomerWalletHistory();
        walletHistory.setCustomerId(customerId);
        walletHistory.setSourceType(SourceType.EB.name());
        walletHistory.setAmount(history.getAmount());
        walletHistory.setTransactionDate(history.getEndDate());
        walletHistory.setTransactionType(WalletTransactionType.CREDIT.name());
        walletHistory.setBillingStatus(WalletBillingStatus.INVOICE_NOT_GENERATED.name());
        walletHistory.setSourceId(String.valueOf(history.getReadingId()));
        walletHistory.setBillStartDate(history.getStartDate());
        walletHistory.setBillEndDate(history.getEndDate());
        walletHistory.setCreatedAt(new Date());
        walletHistory.setCreatedBy(createdBy);

        return walletHistory;

    }

    public void saveAll(List<CustomerWalletHistory> customerWallets) {
        walletHistoryRepository.saveAll(customerWallets);
    }

    public List<CustomerWalletHistory> getWalletListForRecurring(List<String> customerIds) {
        return walletHistoryRepository.findByCustomerIdIn(customerIds);
    }

    public void addReassignRentIntoWalletHistory(double balanceAmount, String invoiceId, String customerId, Date transactionDate) {
        CustomerWalletHistory walletHistory = new CustomerWalletHistory();
        walletHistory.setCustomerId(customerId);
        walletHistory.setSourceType(SourceType.REASSIGN_RENT.name());
        walletHistory.setAmount(balanceAmount);
        walletHistory.setTransactionDate(transactionDate);
        walletHistory.setTransactionType(WalletTransactionType.DEBIT.name());
        walletHistory.setBillingStatus(WalletBillingStatus.INVOICE_NOT_GENERATED.name());
        walletHistory.setSourceId(invoiceId);
        walletHistory.setBillStartDate(null);
        walletHistory.setBillEndDate(null);
        walletHistory.setCreatedAt(new Date());
        walletHistory.setCreatedBy(authentication.getName());

        walletHistoryRepository.save(walletHistory);
    }

    public List<WalletTransactions> getWalletTransactions(String customerId) {
        List<WalletTransactions> walletTransactions = new ArrayList<>();
        List<CustomerWalletHistory> listWalletHistory = walletHistoryRepository.findByCustomerId(customerId);
        if (!listWalletHistory.isEmpty()) {
            walletTransactions = listWalletHistory
                    .stream()
                    .map(i -> new WalltetTransactionMapper().apply(i))
                    .toList();
        }
        return walletTransactions;
    }
}
