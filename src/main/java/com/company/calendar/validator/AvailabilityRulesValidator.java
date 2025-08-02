package com.company.calendar.validator;

import com.company.calendar.dto.availability.AvailabilityRuleSetupRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AvailabilityRulesValidator implements ConstraintValidator<ValidAvailabilityRules, AvailabilityRuleSetupRequest> {

    @Override
    public boolean isValid(AvailabilityRuleSetupRequest request, ConstraintValidatorContext context) {
        List<AvailabilityRuleSetupRequest.AvailabilityRuleRequest> rules = request.getRules();
        if (rules == null) return true;

        Set<String> uniqueKeys = new HashSet<>();

        for (AvailabilityRuleSetupRequest.AvailabilityRuleRequest rule : rules) {
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

