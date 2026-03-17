package com.smartstay.smartstay.Validators;

import com.smartstay.smartstay.annotations.ValidBoolean;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BooleanValidator implements ConstraintValidator<ValidBoolean, Boolean> {
    @Override
    public boolean isValid(Boolean value, ConstraintValidatorContext context) {
        return false;
    }
}
