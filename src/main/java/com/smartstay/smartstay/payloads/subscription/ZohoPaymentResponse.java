package com.smartstay.smartstay.payloads.subscription;


public record ZohoPaymentResponse(String type,
                                  String status,
                                  String linkId,
                                  PaymentStatus upiStatus,
                                  PaymentStatusCardType cardType) {
}
