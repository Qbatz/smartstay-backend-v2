package com.smartstay.smartstay.responses.assets;

public record BankForAssets(String bankId,
                            boolean isBank,
                            String bankName,
                            String accountType,
                            String displayName,
                            String holderName,
                            boolean isUpi,
                            String upiAppIcon) {
}
