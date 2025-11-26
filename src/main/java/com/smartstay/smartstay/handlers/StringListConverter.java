package com.smartstay.smartstay.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class StringListConverter implements AttributeConverter<List<String>, String> {
    private static final ObjectMapper mapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            return "[]";
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return null;
        }
    }
}
