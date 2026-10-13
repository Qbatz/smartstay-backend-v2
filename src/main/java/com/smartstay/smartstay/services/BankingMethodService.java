package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.dto.bank.OldBankToNewBankInfo;
import com.smartstay.smartstay.ennum.PaymentMethod;
import com.smartstay.smartstay.repositories.BankingMethodsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BankingMethodService {
    @Autowired
    private BankingMethodsRepository bankingMethodsRepository;
    @Autowired
    private Authentication authentication;


    public boolean findBankByMethodId(String bankId) {
        if (bankId.startsWith("SSBMB-")) {
            String methodId = bankId.replace("SSBMB-", "");
            return bankingMethodsRepository.findByMethodId(methodId) != null;
        }
        return false;
    }

    public String creditAmount(double paidAmount, String bankId, String transactionDate) {
        if (bankId.startsWith("SSBMB-")) {
            String methodId = bankId.replace("SSBMB-", "");

            BankingMethods bm = bankingMethodsRepository.findByMethodId(methodId);
            if (bm == null) {
                return null;
            }
            Double balanceAmount = bm.getBalance();
            if (balanceAmount == null) {
                bm.setBalance(paidAmount);
            }
            else {
                bm.setBalance(balanceAmount + paidAmount);
            }
            bankingMethodsRepository.save(bm);
            BankingV2 v2 = bm.getBank();
            return v2.getBankId();

        }
        return null;
    }

    public OldBankToNewBankInfo BankInfo(String bankinMethodId) {
        if (bankinMethodId.startsWith("SSBMB-")) {
            String bId = bankinMethodId.replace("SSBMB-", "");
            BankingMethods bankingMethods = bankingMethodsRepository.findByMethodId(bId);
            if (bankingMethods != null) {
                return new OldBankToNewBankInfo(bankingMethods.getBank().getBankId(), bankinMethodId);
            }
        }

        return  null;
    }

    public void updateBankInfo(BankingV2 bank, String bankingMethod, double amount) {
        List<BankingMethods> listBankingMethods = bank
                .getBankingMethods();
        if (listBankingMethods != null) {
            BankingMethods bm = listBankingMethods
                    .stream()
                    .filter(i -> i.getPaymentMethodId().equalsIgnoreCase(bankingMethod))
                    .findFirst()
                    .orElse(null);
            if (bm != null) {
                double balance = 0.0;
                if (bm.getBalance() != null) {
                    balance = bm.getBalance() - amount;
                }
                else {
                    balance = amount * -1;
                }
                bm.setBalance(balance);
                bm.setUpdatedAt(new Date());
                bankingMethodsRepository.save(bm);
            }
        }
    }

    public void addMethodToBankAccount(BankingV2 bankingV2, Double depositAmount, String accountNumber, String displayName, String userId, String hostelId) {
        BankingMethods bm = new BankingMethods();
        bm.setBank(bankingV2);
        bm.setPaymentMethod(PaymentMethod.BANK.name());
        bm.setAccountNumber(accountNumber);
        bm.setDisplayName(displayName);
        bm.setBalance(depositAmount);
        bm.setUserId(userId);
        bm.setHostelId(hostelId);
        bm.setCreatedBy(authentication.getName());
        bm.setCreatedAt(new Date());

        bankingMethodsRepository.save(bm);

    }
}
