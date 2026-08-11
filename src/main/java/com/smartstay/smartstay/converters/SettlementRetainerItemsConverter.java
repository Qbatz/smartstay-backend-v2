package com.smartstay.smartstay.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.smartstay.dto.settlement.EBItems;
import com.smartstay.smartstay.dto.settlement.RetainerItems;
import jakarta.persistence.AttributeConverter;

import javax.management.AttributeList;
import java.util.List;

public class SettlementRetainerItemsConverter implements AttributeConverter<List<RetainerItems>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(List<RetainerItems> retainerItems) {
        try {
            return objectMapper.writeValueAsString(retainerItems);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting to json");
        }
    }

    @Override
    public List<RetainerItems> convertToEntityAttribute(String s) {
        try {
            if (s != null) {
                return objectMapper.readValue(s, new TypeReference<List<RetainerItems>>() {});
            }
            return null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error reading the values");
        }
    }
}
