package com.smartstay.smartstay.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.smartstay.dto.invoices.CancelledInvoice;
import com.smartstay.smartstay.dto.settlement.CurrentOtherItems;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class CancelledInvoiceConverter implements AttributeConverter<List<CancelledInvoice>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(List<CancelledInvoice> cancelledInvoices) {
        try {
            return objectMapper.writeValueAsString(cancelledInvoices);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting to json");
        }
    }

    @Override
    public List<CancelledInvoice> convertToEntityAttribute(String s) {
        try {
            if (s != null) {
                return objectMapper.readValue(s, new TypeReference<List<CancelledInvoice>>() {});
            }
            return null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error reading the values");
        }
    }
}
