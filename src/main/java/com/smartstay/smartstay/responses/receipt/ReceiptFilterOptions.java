package com.smartstay.smartstay.responses.receipt;

import com.smartstay.smartstay.dto.Admin.CommonType;
import com.smartstay.smartstay.filterOptions.invoice.CreatedBy;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ReceiptFilterOptions {
    List<CommonType> invoiceType;
    List<CreatedBy> collectedBy;
    List<CommonType> paymentType;
    List<CommonType> period;

    public ReceiptFilterOptions() {
        period = new ArrayList<>();
        period.add(new CommonType("This month", "THIS_MONTH"));
        period.add(new CommonType("Last month", "LAST_MONTH"));
        period.add(new CommonType("Last 3 months", "LAST_3_MONTHS"));
        period.add(new CommonType("Last 6 months", "LAST_6_MONTHS"));
    }
}
