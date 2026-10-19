package com.smartstay.smartstay.responses.banking;

public record CreditCardInitialization(String cardId,
                                       String cardNumber,
                                       String cardNetwork,
                                       Double payableAmount,
                                       String bankName) {
}
