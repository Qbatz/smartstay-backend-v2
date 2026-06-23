package com.smartstay.smartstay.responses.receipt;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.Admin.CommonType;
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
    }

    public void setCollectedBy(List<Users> listCollectedByUsers) {
        if (listCollectedByUsers != null) {
            collectedBy = listCollectedByUsers
                    .stream()
                    .map(i -> new CreatedBy(NameUtils.getFullName(i.getFirstName(), i.getLastName()), i.getUserId()))
                    .toList();
        }
    }

    public void setPaymentMethod(List<BankingV1> listBanks) {
        if (listBanks != null) {
            paymentMethod = listBanks
                    .stream()
                    .map(i -> new CommonType(i.getAccountType() + " " + i.getAccountHolderName(), i.getBankId()))
                    .toList();
        }
    }
}
