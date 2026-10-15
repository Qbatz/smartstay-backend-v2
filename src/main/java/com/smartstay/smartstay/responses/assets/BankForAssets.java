package com.smartstay.smartstay.responses.assets;

public record BankForAssets(String bankId,
                            String accountType,
                            String displayName,
                            String holderName) {
}
