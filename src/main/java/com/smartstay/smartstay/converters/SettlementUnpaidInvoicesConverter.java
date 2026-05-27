package com.smartstay.smartstay.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.smartstay.dto.settlement.SettlementUnpaidInvoices;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class SettlementUnpaidInvoicesConverter implements AttributeConverter<List<SettlementUnpaidInvoices>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(List<SettlementUnpaidInvoices> settlementUnpaidIInvoices) {
        try {
            return objectMapper.writeValueAsString(settlementUnpaidIInvoices);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting to json");
        }
    }

    @Override
    public List<SettlementUnpaidInvoices> convertToEntityAttribute(String s) {
        try {
            if (s != null) {
                return objectMapper.readValue(s, new TypeReference<>() {
                });
            }
            return null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error reading the values");
        }
    }
}
