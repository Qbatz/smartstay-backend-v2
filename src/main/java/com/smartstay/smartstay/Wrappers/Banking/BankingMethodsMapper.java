package com.smartstay.smartstay.Wrappers.banking;

import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.responses.banking.BankingMethodResponse;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class BankingMethodsMapper implements Function<BankingMethods, BankingMethodResponse> {

    @Override
    public BankingMethodResponse apply(BankingMethods entity) {
        return new BankingMethodResponse(
                entity.getPaymentMethodId(),
                entity.getBank() != null ? entity.getBank().getBankId() : null,
                entity.getPaymentMethod() != null ? entity.getPaymentMethod().getValue() : null,
                entity.getUpiId(),
                entity.getUpiApp(),
                entity.getDisplayName(),
                entity.getDescription(),
                entity.getCardNumber(),
                entity.getCardNetwork(),
                entity.getCardHolderName(),
                entity.getCreditLimit(),
                entity.getBillingCycle() != null ? Utils.dateToString(entity.getBillingCycle()) : null,
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
