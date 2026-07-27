package com.smartstay.smartstay.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoiningDateValidationResult {
    private boolean isValid;
    private String errorCode;
    private String errorMessage;
}
