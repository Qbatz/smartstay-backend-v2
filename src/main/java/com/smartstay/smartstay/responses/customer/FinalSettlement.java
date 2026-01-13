package com.smartstay.smartstay.responses.customer;

import com.smartstay.smartstay.dto.electricity.EBInfo;

import java.util.List;

public record FinalSettlement(CustomerInformations customerInfo,
                              StayInfo stayInfo,
                              EBInfo ebInfo,
                              List<UnpaidInvoices> unpaidInvoices,
                              RentInfo currentMonthRentInfo,
                              SettlementInfo settlementInfo) {
}
