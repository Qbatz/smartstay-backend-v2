package com.smartstay.smartstay.dto.bank;

public record BookingBankInfo(String bankId,
                              String holderName,
                              String bankName,
                              String upiId,
                              boolean isUpi) {
}
