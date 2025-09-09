package com.smartstay.smartstay.Wrappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.dto.invoices.Invoices;
import com.smartstay.smartstay.responses.invoices.InvoicesList;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class InvoiceListMapper implements Function<Invoices, InvoicesList> {
    @Override
    public InvoicesList apply(Invoices invoices) {
        StringBuilder fullNameBuilder = new StringBuilder();
        fullNameBuilder.append(invoices.getFirstName());
        fullNameBuilder.append(" ");
        fullNameBuilder.append(invoices.getLastName());

        ObjectMapper mapper = new ObjectMapper();
        List<Deductions> listDeductions = null;
        try {
             listDeductions = mapper.readValue(
                    invoices.getDeductions(), new TypeReference<List<Deductions>>() {}
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        return new InvoicesList(invoices.getFirstName(),
                invoices.getLastName(),
                fullNameBuilder.toString(),
                invoices.getCustomerId(),
                invoices.getAmount(),
                invoices.getInvoiceId(),
                0.0,
                invoices.getCgst(),
                invoices.getSgst(),
                invoices.getGst(),
                Utils.dateToString(invoices.getCreatedAt()),
                invoices.getCreatedBy(),
                invoices.getHostelId(),
                Utils.dateToString(invoices.getInvoiceGeneratedAt()),
                Utils.dateToString(invoices.getInvoiceDueDate()),
                invoices.getInvoiceType(),
                invoices.getPaymentStatus(),
                Utils.dateToString(invoices.getUpdatedAt()),
                invoices.getInvoiceNumber(),
                listDeductions);
    }
}
