package com.smartstay.smartstay.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.smartstay.dto.settlement.WalltetItems;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class SettlementWalletConverter implements AttributeConverter<List<WalltetItems>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(List<WalltetItems> walltetItems) {
        try {
            return objectMapper.writeValueAsString(walltetItems);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting to json");
        }
    }

    @Override
    public List<WalltetItems> convertToEntityAttribute(String s) {
        try {
            if (s != null) {
                return objectMapper.readValue(s, new TypeReference<List<WalltetItems>>() {});
            }
            return null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error reading the values");
        }
    }
}
