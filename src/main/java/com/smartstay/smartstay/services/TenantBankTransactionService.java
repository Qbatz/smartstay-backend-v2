package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dao.TenantBankTransactions;
import com.smartstay.smartstay.ennum.BankSource;
import com.smartstay.smartstay.ennum.BankTransactionType;
import com.smartstay.smartstay.ennum.TransactionType;
import com.smartstay.smartstay.payloads.retainer.LoadBalance;
import com.smartstay.smartstay.repositories.TenantBankTransactionRepositories;
import com.smartstay.smartstay.repositories.TenantBankingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class TenantBankTransactionService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private TenantBankTransactionRepositories tenantBankTransactionRepositories;
    @Autowired
    private TenantBankService tenantBankService;

    public TenantBankTransactions addRetainerTransaction(InvoicesV1 invoicesV1, LoadBalance loadBalance, Date paymentDate, boolean isRegisteredRelation) {
        Double balanceAmount = tenantBankService.addTransactionToTenantBank(paymentDate, loadBalance.amount(), invoicesV1.getCustomerId(), invoicesV1.getHostelId());

        TenantBankTransactions tenantBankTransactions = new TenantBankTransactions();
        tenantBankTransactions.setTransactionAmount(loadBalance.amount());
        tenantBankTransactions.setTransactionDate(paymentDate);
        tenantBankTransactions.setCustomerId(invoicesV1.getCustomerId());
        tenantBankTransactions.setHostelId(invoicesV1.getHostelId());
        tenantBankTransactions.setSourceId(invoicesV1.getInvoiceId());
        tenantBankTransactions.setPlatform(authentication.getSource());
        tenantBankTransactions.setTransactionType(BankTransactionType.CREDIT.name());
        if (isRegisteredRelation) {
            tenantBankTransactions.setRelationId(loadBalance.relationId());
            tenantBankTransactions.setTypeOfRelation("REGISTERED");
        }
        else {
            tenantBankTransactions.setRelationName(loadBalance.relationName());
            tenantBankTransactions.setRelationMobile(loadBalance.mobile());
            tenantBankTransactions.setTypeOfRelation("UNREGISTERED");
        }
        tenantBankTransactions.setBalanceAmount(balanceAmount);
        tenantBankTransactions.setCreatedBy(authentication.getName());
        tenantBankTransactions.setCreatedAt(new Date());

        return tenantBankTransactionRepositories.save(tenantBankTransactions);

    }

    public void addRetainerTransactionForRedemption(String hostelId, String invoiceId, String customerId, HashMap<String, Double> appliedAmountInvoiceIdMapper, Double appliedAmount, Date appliedDate) {


        List<TenantBankTransactions> listTenantTransactions = new ArrayList<>();
        appliedAmountInvoiceIdMapper.keySet().forEach(item -> {
            Double currentBalance = tenantBankService.adjustRetainerBalance(customerId, appliedAmountInvoiceIdMapper.get(item));

            TenantBankTransactions tenantBankTransactions = new TenantBankTransactions();
            tenantBankTransactions.setTransactionAmount(appliedAmount);
            tenantBankTransactions.setTransactionDate(appliedDate);
            tenantBankTransactions.setCustomerId(customerId);
            tenantBankTransactions.setHostelId(hostelId);
            tenantBankTransactions.setSourceId(item);
            tenantBankTransactions.setPlatform(authentication.getSource());
            tenantBankTransactions.setTransactionType(BankTransactionType.DEBIT.name());
            tenantBankTransactions.setBalanceAmount(currentBalance);
            tenantBankTransactions.setCreatedBy(authentication.getName());
            tenantBankTransactions.setCreatedAt(new Date());

            listTenantTransactions.add(tenantBankTransactions);
        });

        tenantBankTransactionRepositories.saveAll(listTenantTransactions);
    }

    public void addRetainerTransactionForRedemption(String hostelId, String customerId, HashMap<String, Double> appliedAmountInvoiceIdMapper, Date appliedDate) {


        List<TenantBankTransactions> listTenantTransactions = new ArrayList<>();
        appliedAmountInvoiceIdMapper.keySet().forEach(item -> {
            Double currentBalance = tenantBankService.adjustRetainerBalance(customerId, appliedAmountInvoiceIdMapper.get(item));

            TenantBankTransactions tenantBankTransactions = new TenantBankTransactions();
            tenantBankTransactions.setTransactionAmount(appliedAmountInvoiceIdMapper.get(item));
            tenantBankTransactions.setTransactionDate(appliedDate);
            tenantBankTransactions.setCustomerId(customerId);
            tenantBankTransactions.setHostelId(hostelId);
            tenantBankTransactions.setSourceId(item);
            tenantBankTransactions.setPlatform(authentication.getSource());
            tenantBankTransactions.setTransactionType(BankTransactionType.DEBIT.name());
            tenantBankTransactions.setBalanceAmount(currentBalance);
            tenantBankTransactions.setCreatedBy(authentication.getName());
            tenantBankTransactions.setCreatedAt(new Date());

            listTenantTransactions.add(tenantBankTransactions);
        });

        tenantBankTransactionRepositories.saveAll(listTenantTransactions);
    }
}
