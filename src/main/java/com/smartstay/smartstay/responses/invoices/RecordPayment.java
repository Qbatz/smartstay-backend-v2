package com.smartstay.smartstay.responses.invoices;

import com.smartstay.smartstay.responses.banking.PaymentMethodOptionResponse;

import java.util.List;

public record RecordPayment(String invoiceId,
                            String hostelId,
                            String invoiceNumber,
                            Double pendingAmount,
                            Double paidAmount,
                            Double totalAmount,
                            CustomerDetails customerInfo,
                            StayInfo stayInfo,
                            List<BankInfoRecordPayments> accountInfo,
                            List<PaymentMethodOptionResponse> allPaymentMethods) {
}
