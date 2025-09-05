package com.smartstay.smartstay.responses.beds;

public record Bank(String bankingId,
                   String bankName,
                   String accountNumber,
                   String ifscCode,
                   String branchName,
                   String branchCode,
                   String accountHolderName,
                   String transactionType,
                   String upiId,
                   String creditCardNumber,
                   String debitCardNumber,
                   String accountType,
                   boolean isDefault) {

}
