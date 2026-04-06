package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dto.bills.PaymentSummary;
import com.smartstay.smartstay.repositories.PaymentStatusRepository;
import com.smartstay.smartstay.repositories.PaymentSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PaymentSummaryService {

    @Autowired
    PaymentSummaryRepository paymentSummaryRepository;

    @Autowired
    Authentication authentication;

    /**
     * this can be used for advance, invoice
     * @param summary
     * @return
     */

    public int addInvoice(PaymentSummary summary) {
        if (!authentication.isAuthenticated()) {
            return 0;
        }
        com.smartstay.smartstay.dao.PaymentSummary paymentSummary = paymentSummaryRepository.findByCustomerId(summary.customerId());
        if (paymentSummary == null) {
            paymentSummary = new com.smartstay.smartstay.dao.PaymentSummary();
            paymentSummary.setCustomerId(summary.customerId());
            paymentSummary.setCustomerStatus(summary.customerStatus());
            paymentSummary.setCustomerMailId(summary.customerMailId());
            paymentSummary.setCustomerMobile(summary.customerMobile());
            paymentSummary.setHostelId(summary.hostelId());
            paymentSummary.setDebitAmount(summary.amount());
            paymentSummary.setBalance(summary.amount());
            paymentSummary.setLastInvoice(summary.invoiceId());
            paymentSummary.setLastUpdate(new Date());
        }
        else {
            paymentSummary.setLastInvoice(summary.invoiceId());
            paymentSummary.setDebitAmount(paymentSummary.getDebitAmount() + summary.amount());
            paymentSummary.setBalance(paymentSummary.getBalance() - summary.amount());

            paymentSummary.setLastUpdate(new Date());
        }

        paymentSummaryRepository.save(paymentSummary);

        return 1;
    }

    /**
     * this is while adding payment or record payment
     * @param summary
     * @return
     */

    public int addPayment(PaymentSummary summary) {
        if (!authentication.isAuthenticated()) {
            return 0;
        }
        com.smartstay.smartstay.dao.PaymentSummary paymentSummary = paymentSummaryRepository.findByCustomerId(summary.customerId());
        if (paymentSummary == null) {
            paymentSummary = new com.smartstay.smartstay.dao.PaymentSummary();
            paymentSummary.setCustomerId(summary.customerId());
            paymentSummary.setCustomerStatus(summary.customerStatus());
            paymentSummary.setCustomerMailId(summary.customerMailId());
            paymentSummary.setCustomerMobile(summary.customerMobile());
            paymentSummary.setHostelId(summary.hostelId());
            paymentSummary.setCreditAmount(summary.amount());
            paymentSummary.setDebitAmount(0.0);
            paymentSummary.setBalance(-summary.amount());
            paymentSummary.setLastInvoice(summary.invoiceId());
            paymentSummary.setLastPayment(summary.amount());
            paymentSummary.setLastUpdate(new Date());
        }
        else {
            Double creditAmount = paymentSummary.getCreditAmount();
            if (creditAmount == null) {
                creditAmount = 0.0;
            }
            paymentSummary.setLastInvoice(summary.invoiceId());
            paymentSummary.setCreditAmount(creditAmount + summary.amount());
            paymentSummary.setBalance(paymentSummary.getBalance() - summary.amount());
            paymentSummary.setLastUpdate(new Date());
            paymentSummary.setLastPayment(summary.amount());
        }

        paymentSummaryRepository.save(paymentSummary);

        return 1;
    }

    public int deleteReceipt(PaymentSummary summary) {
        if (!authentication.isAuthenticated()) {
            return 0;
        }
        com.smartstay.smartstay.dao.PaymentSummary paymentSummary = paymentSummaryRepository.findByCustomerId(summary.customerId());
        if (paymentSummary == null) {
            paymentSummary = new com.smartstay.smartstay.dao.PaymentSummary();
            paymentSummary.setCustomerId(summary.customerId());
            paymentSummary.setCustomerStatus(summary.customerStatus());
            paymentSummary.setCustomerMailId(summary.customerMailId());
            paymentSummary.setCustomerMobile(summary.customerMobile());
            paymentSummary.setHostelId(summary.hostelId());
            paymentSummary.setCreditAmount(0.0);
            paymentSummary.setDebitAmount(0.0);
            paymentSummary.setBalance(0.0);
            paymentSummary.setLastInvoice(summary.invoiceId());
            paymentSummary.setLastPayment(0.0);
            paymentSummary.setLastUpdate(new Date());
        }
        else {
            Double creditAmount = paymentSummary.getCreditAmount();
            if (creditAmount == null) {
                creditAmount = 0.0;
            }
            paymentSummary.setCreditAmount(creditAmount - summary.amount());
            paymentSummary.setBalance(paymentSummary.getBalance() + summary.amount());

            paymentSummary.setLastUpdate(new Date());
        }

        paymentSummaryRepository.save(paymentSummary);

        return 1;
    }


    public void markInvoiceUnpaid(String customerId, Double totalAmount, String invoiceId) {
        com.smartstay.smartstay.dao.PaymentSummary paymentSummary = paymentSummaryRepository.findByCustomerId(customerId);
        if (paymentSummary == null) {
            paymentSummary = new com.smartstay.smartstay.dao.PaymentSummary();
            paymentSummary.setCustomerId(customerId);
            paymentSummary.setCustomerStatus(paymentSummary.getCustomerStatus());
            paymentSummary.setCustomerMailId(paymentSummary.getCustomerMailId());
            paymentSummary.setCustomerMobile(paymentSummary.getCustomerMobile());
            paymentSummary.setHostelId(paymentSummary.getHostelId());
            paymentSummary.setDebitAmount(totalAmount);
            paymentSummary.setBalance((totalAmount * -1));
            paymentSummary.setLastInvoice(invoiceId);
            paymentSummary.setLastUpdate(new Date());
        }
        else {
            paymentSummary.setLastInvoice(invoiceId);
            paymentSummary.setDebitAmount(paymentSummary.getDebitAmount() + totalAmount);
            paymentSummary.setBalance(paymentSummary.getBalance() - totalAmount);

            paymentSummary.setLastUpdate(new Date());
        }

        paymentSummaryRepository.save(paymentSummary);
    }

    public void applyDiscount(String customerId, double discountAmount) {
        com.smartstay.smartstay.dao.PaymentSummary paymentSummary = paymentSummaryRepository.findByCustomerId(customerId);
        if (paymentSummary != null) {
            paymentSummary.setDebitAmount(paymentSummary.getDebitAmount() - discountAmount);
            paymentSummary.setBalance(paymentSummary.getBalance() - discountAmount);
            paymentSummary.setLastUpdate(new Date());

            paymentSummaryRepository.save(paymentSummary);
        }
    }

    public void editDiscount(String customerId, double difference) {
        com.smartstay.smartstay.dao.PaymentSummary paymentSummary = paymentSummaryRepository.findByCustomerId(customerId);
        if (paymentSummary != null) {
            paymentSummary.setDebitAmount(paymentSummary.getDebitAmount() - difference);
            paymentSummary.setBalance(paymentSummary.getBalance() - difference);
            paymentSummary.setLastUpdate(new Date());

            paymentSummaryRepository.save(paymentSummary);
        }
    }

    public void deleteDiscount(String customerId, Double invoiceDiscountAmount) {
        com.smartstay.smartstay.dao.PaymentSummary paymentSummary = paymentSummaryRepository.findByCustomerId(customerId);
        if (paymentSummary != null) {
            paymentSummary.setDebitAmount(paymentSummary.getDebitAmount() + invoiceDiscountAmount);
            paymentSummary.setBalance(paymentSummary.getBalance() + invoiceDiscountAmount);
            paymentSummary.setLastUpdate(new Date());

            paymentSummaryRepository.save(paymentSummary);
        }
    }
}
