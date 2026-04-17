package com.smartstay.smartstay.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.smartstay.dao.TenantFilters;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.lang.reflect.Type;
import java.util.List;

@Converter
public class TenantFilterConverters implements AttributeConverter<List<TenantFilters>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(List<TenantFilters> attribute) {

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TenantFilters> convertToEntityAttribute(String data) {
        try {
            return objectMapper.readValue(data, new TypeReference<List<TenantFilters>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
