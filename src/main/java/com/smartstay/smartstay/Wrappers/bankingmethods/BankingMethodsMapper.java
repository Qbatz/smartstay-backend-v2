package com.smartstay.smartstay.Wrappers.bankingmethods;

import com.smartstay.smartstay.config.UPITypes;
import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.responses.banking.PaymentMethodOptionResponse;

import java.util.function.Function;

public class BankingMethodsMapper implements Function<BankingMethods, PaymentMethodOptionResponse> {
    private UPITypes upiTypes;

    public BankingMethodsMapper(UPITypes upiTypes) {
        this.upiTypes = upiTypes;
    }

    @Override
    public PaymentMethodOptionResponse apply(BankingMethods bankingMethods) {
        String bankId = null;
        BankingV2 bankingV2 = bankingMethods.getBank();
        String bankName = null;
        String branchName = null;
        String accountHolder = null;
        String ifscCode = null;
        String responsiblePerson = null;
        String responsiblePersonId = null;
        String upiAppIcon = upiTypes.getUPIType(bankingMethods);

        if (bankingV2 != null) {
            bankId = bankingV2.getBankId();
            accountHolder = bankingV2.getAccountHolderName();
            bankName = bankingV2.getBankName();
            branchName = bankingV2.getBranchName();
            ifscCode = bankingV2.getIfscCode();
            responsiblePersonId = bankingV2.getResponsiblePerson();
            responsiblePerson = "NA";
        }

        return new PaymentMethodOptionResponse(bankId,
                accountHolder,
                bankingMethods.getAccountNumber(),
                bankingMethods.getPaymentMethod(),
                bankingMethods.getBalance(),
                bankName,
                branchName,
                bankingMethods.getDisplayName(),
                ifscCode,
                responsiblePersonId,
                responsiblePerson,
                bankingMethods.getPaymentMethodId(),
                bankingMethods.getCardNumber(),
                bankingMethods.getUpiId(),
                bankingMethods.getCardHolderName(),
                bankingMethods.getCardNetwork(),
                bankingMethods.getUpiApp(),
                upiAppIcon);
    }
}
