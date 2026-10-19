package com.smartstay.smartstay.config;

import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.ennum.PaymentMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Service
public class UPITypes {
    @Value("${BHIM_UPI}")
    private String paymentTypesUPI;
    @Value("${UPI_TYPES_GPAY}")
    private String paymentTypesGpay;
    @Value("${UPI_TYPES_PHONE_PE}")
    private String paymentTypesPhonePe;
    @Value("${UPI_TYPES_PAYTM}")
    private String paymentTypesPayTm;
    @Value("${UPI_TYPES_AMAZON}")
    private String paymentTypesAmazonPe;

    public String getUPIType(BankingMethods bm) {
        if (bm == null) {
            return null;
        }
        if (bm.getPaymentMethod().equalsIgnoreCase(PaymentMethod.UPI.name())) {
            if (bm.getUpiApp() == null) {
                return paymentTypesUPI;
            }
            if (bm.getUpiApp().equalsIgnoreCase("GPAY")) {
                return paymentTypesGpay;
            }
            if (bm.getUpiApp().equalsIgnoreCase("PHONE_PE")) {
                return paymentTypesPhonePe;
            }
            if (bm.getUpiApp().equalsIgnoreCase("AMAZON_PAY")) {
                return paymentTypesAmazonPe;
            }
            if (bm.getUpiApp().equalsIgnoreCase("PAYTM")) {
                return paymentTypesPayTm;
            }
        }
        return paymentTypesUPI;
    }
}
