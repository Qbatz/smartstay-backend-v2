package com.smartstay.smartstay.payloads.banking;

public record MoneyTransferV2(
        String fromBankId,
        String toBankId,
        Double amount
) {
}
