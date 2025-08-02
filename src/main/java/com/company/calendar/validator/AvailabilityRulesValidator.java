package com.company.calendar.validator;

import com.company.calendar.dto.availability.AvailabilitySetupRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AvailabilityRulesValidator implements ConstraintValidator<ValidAvailabilityRules, AvailabilitySetupRequest> {

    @Override
    public boolean isValid(AvailabilitySetupRequest request, ConstraintValidatorContext context) {
        List<AvailabilitySetupRequest.AvailabilityRuleRequest> rules = request.getRules();
        if (rules == null) return true;

        Set<String> uniqueKeys = new HashSet<>();

        for (AvailabilitySetupRequest.AvailabilityRuleRequest rule : rules) {
            if (rule.getStartTime() == null || rule.getEndTime() == null || rule.getDayOfWeek() == null) {
                continue;
            }

            if (!rule.getStartTime().isBefore(rule.getEndTime())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Start time must be before end time")
                        .addConstraintViolation();
                return false;
            }

            String key = rule.getDayOfWeek() + "|" + rule.getStartTime() + "|" + rule.getEndTime() + "|" + rule.getRuleType();
            if (!uniqueKeys.add(key)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Duplicate time window for same day and rule type")
                        .addConstraintViolation();
                return false;
            }
        }

        // Optional: Add more logic for overlapping time windows
        return true;
    }
}

