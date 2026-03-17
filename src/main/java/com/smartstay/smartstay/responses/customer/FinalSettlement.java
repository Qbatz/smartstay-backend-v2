package com.smartstay.smartstay.responses.customer;

import com.smartstay.smartstay.dto.electricity.EBInfo;
import com.smartstay.smartstay.dto.wallet.WalletInfo;

import java.util.List;

public record FinalSettlement(CustomerInformations customerInfo,
                              StayInfo stayInfo,
                              EBInfo ebInfo,
                              List<UnpaidInvoices> unpaidInvoices,
                              RentInfo currentMonthRentInfo,
                              WalletInfo walletInfo,
                              SettlementInfo settlementInfo) {
}
