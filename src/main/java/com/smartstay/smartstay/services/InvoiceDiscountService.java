package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.InvoiceDiscounts;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dto.invoices.InvoiceDiscountDto;
import com.smartstay.smartstay.payloads.invoice.ApplyDiscount;
import com.smartstay.smartstay.repositories.InvoiceDiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class InvoiceDiscountService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private InvoiceDiscountRepository invoiceDiscountRepository;
    public void applyDiscount(String hostelId, String invoiceId, String customerId, ApplyDiscount discount, double discountAmount, Double totalInvoiceAmount) {
        double percentage = (discountAmount/totalInvoiceAmount) * 100;
        InvoiceDiscounts invoiceDiscounts = new InvoiceDiscounts();
        invoiceDiscounts.setInvoiceId(invoiceId);
        invoiceDiscounts.setHostelId(hostelId);
        invoiceDiscounts.setCustomerId(customerId);
        invoiceDiscounts.setDiscountReason(discount.reason());
        invoiceDiscounts.setDiscountAmount(discountAmount);
        invoiceDiscounts.setDiscountPercentage(percentage);
        invoiceDiscounts.setInvoiceAmount(totalInvoiceAmount);
        invoiceDiscounts.setActive(true);
        invoiceDiscounts.setCreatedAt(new Date());
        invoiceDiscounts.setCreatedBy(authentication.getName());

        invoiceDiscountRepository.save(invoiceDiscounts);
    }

    public com.smartstay.smartstay.dto.invoices.InvoiceDiscounts getInvoiceDiscounts(String hostelId, String invoiceId) {
        return invoiceDiscountRepository.findFirstByHostelIdAndInvoiceIdOrderByDiscountIdDesc(hostelId, invoiceId)
                .map(d -> new com.smartstay.smartstay.dto.invoices.InvoiceDiscounts(
                        d.getDiscountReason(),
                        d.getDiscountPercentage(),
                        d.getDiscountAmount()
                )).orElse(null);
    }

    public InvoiceDiscountDto editDiscount(String hostelId, String invoiceId, ApplyDiscount discount) {
        InvoiceDiscounts invDiscount = invoiceDiscountRepository.findByHostelIdAndInvoiceId(hostelId, invoiceId);
        double oldInvoiceDiscount = 0.0;
        if (invDiscount != null) {
            oldInvoiceDiscount = invDiscount.getDiscountAmount();
            double discountAmount = 0.0;
            double discountPercentage = 0.0;
            if (discount.discountPercentage() != null) {
                discountPercentage = Double.parseDouble(String.valueOf(discount.discountPercentage()));
                discountAmount = (double) Math.round(((double) discountPercentage / 100) * invDiscount.getInvoiceAmount());
            }
            else if (discount.discountAmount() != null) {
                discountAmount = discount.discountAmount();
                discountPercentage =  (discountAmount/invDiscount.getInvoiceAmount()) * 100;
            }

            if (discount.reason() != null) {
                invDiscount.setDiscountReason(discount.reason());
            }
            else if (invDiscount.getDiscountReason() != null) {
                invDiscount.setDiscountReason(null);
            }

            invDiscount.setDiscountPercentage(discountPercentage);
            invDiscount.setDiscountAmount(discountAmount);

            invoiceDiscountRepository.save(invDiscount);

            double difference = oldInvoiceDiscount - discountAmount;

            return new InvoiceDiscountDto(discountPercentage,
                    discountAmount,
                    oldInvoiceDiscount,
                    difference);
        }
        return null;
    }

    public double deleteInvoiceDiscount(String hostelId, String invoiceId) {
        InvoiceDiscounts invoiceDiscounts = invoiceDiscountRepository.findByHostelIdAndInvoiceId(hostelId, invoiceId);
        if (invoiceDiscounts != null) {
            invoiceDiscounts.setActive(false);
            invoiceDiscountRepository.save(invoiceDiscounts);
            return invoiceDiscounts.getDiscountAmount();
        }
        return 0.0;
    }

    public List<InvoiceDiscounts> getInvoiceDiscounts(String hostelId, List<String> invoiceIds) {
        List<InvoiceDiscounts> listInvoiceDiscounts = invoiceDiscountRepository.findByHostelIdAndInvoiceIdIn(hostelId, invoiceIds);
        if (listInvoiceDiscounts == null) {
            listInvoiceDiscounts = new ArrayList<>();
        }

        return listInvoiceDiscounts;
    }

    public Double getDiscountAmount(String hostelId,String invoiceId) {
        InvoiceDiscounts discount = invoiceDiscountRepository.findByHostelIdAndInvoiceIdAndIsActiveTrue(hostelId, invoiceId);
        if (discount == null) {
            return 0.0;
        }
        return discount.getDiscountAmount();
    }

    public double getDiscountAmount(String hostelId, List<String> discountedInvoices) {
        double discountAmount = 0.0;
        List<InvoiceDiscounts> listDiscounts = invoiceDiscountRepository
                .findByHostelIdAndInvoiceIdsAndIsActive(hostelId, discountedInvoices);

        if (listDiscounts != null && !listDiscounts.isEmpty()) {
            discountAmount = listDiscounts
                    .stream()
                    .mapToDouble(i -> {
                        if (i.getDiscountAmount() == null) {
                            return 0.0;
                        }
                        return i.getDiscountAmount();
                    })
                    .sum();
        }

        return discountAmount;
    }
}
