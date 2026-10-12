package com.smartstay.smartstay.responses.receipt;

import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.Admin.CommonType;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.filterOptions.invoice.CreatedBy;
import com.smartstay.smartstay.util.NameUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ReceiptFilterOptions {
    List<CommonType> invoiceType;
    List<CreatedBy> collectedBy;
    List<CommonType> period;
    List<CommonType> paymentMethod;

    public ReceiptFilterOptions() {
        period = new ArrayList<>();
        period.add(new CommonType("This month", "THIS_MONTH"));
        period.add(new CommonType("Last month", "LAST_MONTH"));
        period.add(new CommonType("Last 3 months", "LAST_3_MONTHS"));
        period.add(new CommonType("Last 6 months", "LAST_6_MONTHS"));
        period.add(new CommonType("Custom", "CUSTOM"));

        invoiceType = new ArrayList<>();
        invoiceType.add(new CommonType("Rent", "RENT"));
        invoiceType.add(new CommonType("Advance", "ADVANCE"));
        invoiceType.add(new CommonType("Settlement", "SETTLEMENT"));
        invoiceType.add(new CommonType("Booking", "BOOKING"));
        invoiceType.add(new CommonType("Advance Holding", "AMOUNT_HOLDING"));
        invoiceType.add(new CommonType("EB Holding", "EB_HOLDING"));
        invoiceType.add(new CommonType("Other", InvoiceType.OTHER.name()));
    }

    public void setCollectedBy(List<Users> listCollectedByUsers) {
        if (listCollectedByUsers != null) {
            collectedBy = listCollectedByUsers
                    .stream()
                    .map(i -> new CreatedBy(NameUtils.getFullName(i.getFirstName(), i.getLastName()), i.getUserId()))
                    .toList();
        }
    }

//    public void setPaymentMethod(List<BankingV1> listBanks) {
//        if (listBanks != null) {
//            paymentMethod = listBanks
//                    .stream()
//                    .map(i -> new CommonType( i.getAccountHolderName() + "-" + i.getAccountType(), i.getBankId()))
//                    .toList();
//        }
//    }

    public void setPaymentMethod(List<BankingV2> listBanks) {
        if (listBanks != null) {
            List<CommonType> commonTypes = new ArrayList<>();
            List<BankingV2> cashAndCreditCards = listBanks
                    .stream()
                    .filter(i -> i.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name()) || i.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name()))
                    .toList();
            if (cashAndCreditCards != null) {
                List<CommonType> cardsAndCash = cashAndCreditCards
                        .stream()
                        .map(i -> new CommonType( i.getAccountHolderName() + "-" + i.getAccountType(), i.getBankId()))
                        .toList();
                commonTypes.addAll(cardsAndCash);
            }

            List<BankingV2> upiBankAndDebitCards = listBanks
                    .stream()
                    .filter(i -> i.getAccountType().equalsIgnoreCase(BankAccountType.BANK.name()))
                    .toList();
            if (upiBankAndDebitCards != null) {
                List<BankingMethods> bankingMethods = new ArrayList<>();
                upiBankAndDebitCards.forEach(i2 -> {
                    bankingMethods.addAll(i2.getBankingMethods());
                });

                if (bankingMethods != null) {
                    commonTypes.addAll(bankingMethods
                            .stream()
                            .map(i -> new CommonType(i.getDisplayName() + "-" + i.getPaymentMethod(), i.getPaymentMethodId()))
                            .toList());
                }
            }


//            paymentMethod = listBanks
//                    .stream()
//                    .map(i -> new CommonType( i.getAccountHolderName() + "-" + i.getAccountType(), i.getBankId()))
//                    .toList();
        }
    }
}
