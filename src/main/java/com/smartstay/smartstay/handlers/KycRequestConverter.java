package com.smartstay.smartstay.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.smartstay.dto.kyc.RequestKyc;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class KycRequestConverter implements AttributeConverter<RequestKyc, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(RequestKyc attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RequestKyc convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, new TypeReference<RequestKyc>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
