package com.company.calendar.validator;

import com.company.calendar.dto.availability.AvailabilityRuleSetupRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class AvailabilityRulesValidator implements ConstraintValidator<ValidAvailabilityRules, AvailabilityRuleSetupRequest> {

    @Override
    public boolean isValid(AvailabilityRuleSetupRequest request, ConstraintValidatorContext context) {
        List<AvailabilityRuleSetupRequest.AvailabilityRuleRequest> rules = request.getRules();
        if (CollectionUtils.isEmpty(rules)) {
            log.debug("Validation failed: Availability rules list is empty.");
            return false;
        }

        var uniqueKeys = new HashSet<>();

        for (AvailabilityRuleSetupRequest.AvailabilityRuleRequest rule : rules) {
            if (rule.getStartTime() == null || rule.getEndTime() == null || rule.getDayOfWeek() == null) {
                log.debug("Validation failed: Missing required fields in rule: {}", rule);
                return false;
            }

            var start = rule.getStartTime();
            var end = rule.getEndTime();

            //1. Must be on-the-hour (e.g., 14:00, not 14:30)
            if (start.getMinute() != 0 || end.getMinute() != 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Start and end time must be at full hour")
                        .addConstraintViolation();
                return false;
            }

            //2. Start must be before or equal to end
            if (start.isAfter(end)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Start time must not be after end time")
                        .addConstraintViolation();
                log.debug("Validation failed: Start time is after end time - start: {}, end: {}", start, end);
                return false;
            }

            //3. No duplicate rules
            String key = rule.getDayOfWeek() + "|" + start + "|" + end;
            if (!uniqueKeys.add(key)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Duplicate time window for same day and rule type")
                        .addConstraintViolation();
                log.debug("Validation failed: Duplicate rule found for key: {}", key);
                return false;
            }
        }
        // All rules passed
        log.debug("Validation passed: All availability rules are valid.");
        return true;
    }
}
