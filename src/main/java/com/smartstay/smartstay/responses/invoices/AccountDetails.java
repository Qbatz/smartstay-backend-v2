package com.smartstay.smartstay.responses.invoices;

public record AccountDetails(String accountNo,
                             String ifscCode,
                             String bankName,
                             String upiId,
                             String qrCode) {
}
