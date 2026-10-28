package com.smartstay.smartstay.dto.bank;

public record BookingBankInfo(String bankId,
                              boolean isBank,
                              String bankName,
                              String accountType,
                              String displayName,
                              String accountHolderName,
                              boolean isUpi,
                              String upiIcon) {
}
