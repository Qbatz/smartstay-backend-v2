package com.smartstay.smartstay.responses.invoices;

import java.util.List;

public record FinalSettlementResponse(String invoiceNumber,
                                      String invoiceId,
                                      String invoiceDate,
                                      String dueDate,
                                      String emailId,
                                      String mobile,
                                      String countryCode,
                                      String invoiceType,
                                      CustomerInfo customerInfo,
                                      StayInfo stayInfo,
                                      AccountDetails accountDetails,
                                      ConfigInfo configurations,

                                      List<InvoiceSummary> invoiceSummaries
) {
}
