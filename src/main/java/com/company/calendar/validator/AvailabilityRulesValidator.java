package com.company.calendar.validator;

import com.company.calendar.dto.availability.AvailabilityRuleSetupRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalTime;
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

            LocalTime start = rule.getStartTime();
            LocalTime end = rule.getEndTime();

            //1. Must be on-the-hour (e.g., 14:00, not 14:30)
            if (start.getMinute() != 0 || end.getMinute() != 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Start and end time must be at full hour")
                        .addConstraintViolation();
                return false;
            }

            //2. Must be between 00:00 and 23:00
            if (start.getHour() < 0 || start.getHour() > 23 || end.getHour() < 0 || end.getHour() > 23) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Start and end hour must be between 00:00 and 23:00")
                        .addConstraintViolation();
                return false;
            }

            //3. Start must be before end
            if (!start.isBefore(end)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Start time must be before end time")
                        .addConstraintViolation();
                return false;
            }

            //4. No duplicate rules
            String key = rule.getDayOfWeek() + "|" + start + "|" + end;
            if (!uniqueKeys.add(key)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Duplicate time window for same day and rule type")
                        .addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
