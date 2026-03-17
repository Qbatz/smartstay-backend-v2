package com.smartstay.smartstay.responses.invoices;

import com.smartstay.smartstay.dto.customer.InvoiceRefundHistory;

import java.util.List;

public record FinalSettlementResponse(String invoiceNumber,
                                      String invoiceId,
                                      String invoiceDate,
                                      String dueDate,
                                      String emailId,
                                      String mobile,
                                      String countryCode,
                                      String invoiceType,
                                      String hostelId,
                                      CustomerInfo customerInfo,
                                      StayInfo stayInfo,
                                      AccountDetails accountDetails,
                                      ConfigInfo configurations,

                                      List<InvoiceSummary> invoiceSummaries,
                                      InvoiceInfo invoiceInfo,
                                      List<InvoiceRefundHistory> refundHistory,
                                      List<InvoiceRefundHistory> paymentHistory
) {
}
