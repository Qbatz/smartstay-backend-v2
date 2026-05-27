package com.smartstay.smartstay.responses.customer;

import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.dto.electricity.EBInfo;
import com.smartstay.smartstay.dto.wallet.WalletInfo;
import com.smartstay.smartstay.responses.settlement.DeductionsInfo;

import java.util.List;

public record FinalSettlement(CustomerInformations customerInfo,
                              StayInfo stayInfo,
                              EBInfo ebInfo,
                              List<UnpaidInvoices> unpaidInvoices,
                              com.smartstay.smartstay.responses.settlement.UnpaidInvoices unpaidInvoiceInfo,
                              RentInfo currentMonthRentInfo,
                              WalletInfo walletInfo,
                              AdvanceItems advanceItems,
                              AdvanceItems bookingItems,
                              DeductionsInfo deductionsInfo,
                              SettlementInfo settlementInfo) {
}
