package com.smartstay.smartstay.responses.settlement;


import com.smartstay.smartstay.Wrappers.settlement.CurrentMonthEbInfo;
import com.smartstay.smartstay.Wrappers.settlement.CurrentRentInfo;
import com.smartstay.smartstay.responses.customer.AdvanceItems;
import com.smartstay.smartstay.responses.customer.RentInfo;
import com.smartstay.smartstay.responses.invoices.*;

public record FinalSettlementInvoice(StayInfo stayInfo,
                                     AccountDetails accountDetails,
                                     ConfigInfo configInfo,
                                     CustomerInfo customerInfo,
                                     UnpaidInvoiceInfo unpaidInvoiceInfo,
                                     DeductionsInfo deductionsInfo,
                                     AdvanceItems advanceItems,
                                     AdvanceItems bookingItems,
                                     CurrentRentInfo currentMonthRentInfo,
                                     CurrentMonthEbInfo currentMonthEbInfo) {
}
