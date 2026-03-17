package com.smartstay.smartstay.annotations;

import com.smartstay.smartstay.Validators.BooleanValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BooleanValidator.class)
public @interface ValidBoolean {
    String message() default "Invalid boolean value. Only true/false allowed.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
