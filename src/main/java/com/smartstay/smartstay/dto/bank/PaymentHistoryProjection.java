package com.smartstay.smartstay.dto.bank;

import java.util.Date;

public interface PaymentHistoryProjection {
    String getReferenceNumber();
    Double getAmount();
    String getPaidDate();
}
