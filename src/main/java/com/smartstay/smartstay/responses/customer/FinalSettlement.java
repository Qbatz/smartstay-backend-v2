package com.smartstay.smartstay.responses.customer;

import java.util.List;

public record FinalSettlement(CustomerInformations customerInfo,
                              StayInfo stayInfo,
                              List<UnpaidInvoices> unpaidInvoices,
                              RentInfo currentMonthRentInfo,
                              SettlementInfo settlementInfo) {
}
