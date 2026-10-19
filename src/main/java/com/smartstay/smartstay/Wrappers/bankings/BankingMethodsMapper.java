package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.config.UPITypes;
import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.ennum.PaymentMethod;
import com.smartstay.smartstay.responses.banking.BankingMethodResponse;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

public class BankingMethodsMapper implements Function<BankingMethods, BankingMethodResponse> {
    private UPITypes upiTypes;

    public BankingMethodsMapper(UPITypes upiTypes) {
        this.upiTypes = upiTypes;
    }

    @Override
    public BankingMethodResponse apply(BankingMethods entity) {
        String upiAppImage = null;
        if (entity.getPaymentMethod().equalsIgnoreCase(PaymentMethod.UPI.name())) {
            upiAppImage = upiTypes.getUPIType(entity);
        }
        return new BankingMethodResponse(
                entity.getPaymentMethodId(),
                entity.getBank() != null ? entity.getBank().getBankId() : null,
                entity.getPaymentMethod() != null ? entity.getPaymentMethod() : null,
                entity.getUpiId(),
                entity.getUpiApp(),
                upiAppImage,
                entity.getDisplayName(),
                entity.getDescription(),
                entity.getCardNumber(),
                entity.getCardNetwork(),
                entity.getCardHolderName(),
                entity.getCreditLimit(),
                entity.getBillingCycle(),
                entity.getLinkedUpiId(),
                entity.getQrImage(),
                entity.getHostelId(),
                entity.getUserId(),
                entity.getBalance(),
                Utils.dateToTableFormat(entity.getCreatedAt()),
                Utils.dateToTableFormat(entity.getUpdatedAt()),
                entity.getCreatedBy(),
                entity.getUpdatedBy());
    }
}
