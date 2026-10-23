package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.bankingmethods.BankingMethodsMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.UPITypes;
import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.dto.bank.OldBankToNewBankInfo;
import com.smartstay.smartstay.ennum.PaymentMethod;
import com.smartstay.smartstay.repositories.BankingMethodsRepository;
import com.smartstay.smartstay.responses.banking.PaymentMethodOptionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class BankingMethodService {
    @Autowired
    private BankingMethodsRepository bankingMethodsRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private UPITypes upiTypes;


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
                return new OldBankToNewBankInfo(bankingMethods.getBank().getBankId(), bankingMethods.getPaymentMethodId());
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
                    balance = bm.getBalance() + amount;
                }
                else {
                    balance = amount;
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

    public void addMethodToCreditCard(BankingV2 bankingV2, Double depositAmount, String cardNumber, String displayName, String userId, String hostelId, Double creditLimit, Integer billingCycle, String cardNetwork) {
        BankingMethods bm = new BankingMethods();
        bm.setBank(bankingV2);
        bm.setPaymentMethod(PaymentMethod.CREDIT_CARD.name());
        bm.setCardNumber(cardNumber);
        bm.setDisplayName(displayName);
        bm.setCardNetwork(cardNetwork);
        bm.setBalance(depositAmount);
        bm.setUserId(userId);
        bm.setHostelId(hostelId);
        bm.setCreditLimit(creditLimit);
        bm.setBillingCycle(billingCycle);
        bm.setCreatedBy(authentication.getName());
        bm.setCreatedAt(new Date());

        bankingMethodsRepository.save(bm);

    }

    public List<PaymentMethodOptionResponse> getPaymentMethodsForCreditCard(String hostelId) {
        List<BankingMethods> listBankingMethods = bankingMethodsRepository.findAllPaymentMethods(hostelId);
        if (listBankingMethods == null) {
            return new ArrayList<>();
        }

        return listBankingMethods
                .stream()
                .map(i -> new BankingMethodsMapper(upiTypes).apply(i))
                .toList();
    }

    public BankingV2 findBankAccountByMethodId(String paymentMethodId) {
        BankingMethods bankingMethods = bankingMethodsRepository.findByMethodId(paymentMethodId);
        if (bankingMethods == null) {
            return null;
        }
        return bankingMethods.getBank();
    }

    public void creditCreditCardPayment(BankingV2 bankingV2ToAccount, double amount, Date settlementDate) {
        if (bankingV2ToAccount.getBankingMethods() != null) {
            BankingMethods bm = bankingV2ToAccount.getBankingMethods()
                    .stream()
                    .findFirst()
                    .orElse(null);
            if (bm != null) {
                double balance = 0.0;
                if (bm.getBalance() != null) {
                    balance = bm.getBalance() + amount;
                }
                else {
                    balance = amount;
                }

                bm.setBalance(balance);
                bankingMethodsRepository.save(bm);
            }
        }
    }

    public void creditDebitPayment(String paymentMethodId, double amount, Date settlementDate) {
        BankingMethods bm = bankingMethodsRepository.findByMethodId(paymentMethodId);
        if (bm != null) {
            double balance = 0.0;
            if (bm.getBalance() != null) {
                balance = bm.getBalance() - amount;
            }
            else {
                balance = amount;
            }
            bm.setBalance(balance);
            bankingMethodsRepository.save(bm);
        }
    }

    public void deleteAllBankingMethods(String bankId) {
        List<BankingMethods> listBankingMethods = bankingMethodsRepository.findMethodsByBankId(bankId);
        if (listBankingMethods != null) {
            List<BankingMethods> newBankingMethods = listBankingMethods
                    .stream()
                    .map(i -> {
                        i.setIsDeleted(true);
                        return i;
                    })
                    .toList();
            bankingMethodsRepository.deleteAll(newBankingMethods);
        }
    }

    public void deleteReceipt(Double paidAmount, String paymentMethodId) {
        BankingMethods bm = bankingMethodsRepository.findByMethodId(paymentMethodId);
        if (bm != null) {
            if (bm.getBalance() == null) {
                bm.setBalance(-1 * paidAmount);
            }
            else {
                bm.setBalance(bm.getBalance() - paidAmount);
            }

            bm.setUpdatedAt(new Date());
            bm.setUpdatedBy(authentication.getName());
            bankingMethodsRepository.save(bm);
        }
    }

    public boolean isExists(String hostelId, String cardNumber) {
        BankingMethods bm = bankingMethodsRepository.findByHostelIdAndCardNumber(hostelId, cardNumber);
        if (bm == null) {
            return true;
        }
        return false;
    }
}
