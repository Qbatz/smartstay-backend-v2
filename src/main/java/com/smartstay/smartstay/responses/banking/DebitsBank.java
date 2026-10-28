package com.smartstay.smartstay.responses.banking;

public record DebitsBank(String bankId,
                         boolean isBank,
                         String bankName,
                         String accountType,
                         String displayName,
                         String accountHolderName,
                         String upiIcons) {
}
