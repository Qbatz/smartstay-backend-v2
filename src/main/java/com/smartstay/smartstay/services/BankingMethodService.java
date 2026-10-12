package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.dto.bank.OldBankToNewBankInfo;
import com.smartstay.smartstay.repositories.BankingMethodsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BankingMethodService {
    @Autowired
    private BankingMethodsRepository bankingMethodsRepository;


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
        BankingMethods bankingMethods = bankingMethodsRepository.findByMethodId(bankinMethodId);
        if (bankingMethods != null) {
            return new OldBankToNewBankInfo(bankingMethods.getBank().getBankId(), bankinMethodId);
        }
        return  null;
    }
}
