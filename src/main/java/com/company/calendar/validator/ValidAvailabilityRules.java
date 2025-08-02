package com.company.calendar.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AvailabilityRulesValidator.class)
@Documented
public @interface ValidAvailabilityRules {
    String message() default "Invalid availability rules";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
