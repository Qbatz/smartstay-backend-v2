package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.InvoiceDiscounts;
import com.smartstay.smartstay.payloads.invoice.ApplyDiscount;
import com.smartstay.smartstay.repositories.InvoiceDiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class InvoiceDiscountService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private InvoiceDiscountRepository invoiceDiscountRepository;
    public void applyDiscount(String hostelId, String invoiceId, String customerId, ApplyDiscount discount, Double totalInvoiceAmount) {
        double percentage = (discount.discountAmount()/totalInvoiceAmount) * 100;
        InvoiceDiscounts invoiceDiscounts = new InvoiceDiscounts();
        invoiceDiscounts.setInvoiceId(invoiceId);
        invoiceDiscounts.setHostelId(hostelId);
        invoiceDiscounts.setCustomerId(customerId);
        invoiceDiscounts.setDiscountReason(discount.reason());
        invoiceDiscounts.setDiscountAmount(discount.discountAmount());
        invoiceDiscounts.setDiscountPercentage(percentage);
        invoiceDiscounts.setInvoiceAmount(totalInvoiceAmount);
        invoiceDiscounts.setActive(true);
        invoiceDiscounts.setCreatedAt(new Date());
        invoiceDiscounts.setCreatedBy(authentication.getName());

        invoiceDiscountRepository.save(invoiceDiscounts);
    }

    public com.smartstay.smartstay.dto.invoices.InvoiceDiscounts getInvoiceDiscounts(String hostelId, String invoiceId) {
        InvoiceDiscounts discounts = invoiceDiscountRepository.findByHostelIdAndInvoiceId(hostelId, invoiceId);
        if (discounts != null) {
            com.smartstay.smartstay.dto.invoices.InvoiceDiscounts invoiceDiscounts = new com.smartstay.smartstay.dto.invoices.InvoiceDiscounts(discounts.getDiscountReason(),
                    discounts.getDiscountPercentage(),
                    discounts.getDiscountAmount());
            return invoiceDiscounts;
        }

        return null;
    }
}
