package com.c1se_01.roomiego.validator;

import com.c1se_01.roomiego.annotation.ValidFilter;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterValidator implements ConstraintValidator<ValidFilter, String> {

    // operator is captured in group 2
    private static final Pattern TOKEN_PATTERN = Pattern.compile("^\\s*([^:<>~,]+?)\\s*(:>|:<|:|>|<|~)\\s*(.+)$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) return true; // optional
        String[] parts = value.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;

            Matcher m = TOKEN_PATTERN.matcher(part);
            if (!m.matches()) return false;

            String field = m.group(1).trim();
            String val = m.group(3).trim();
            if (field.isEmpty() || val.isEmpty()) return false;
        }
        return true;
    }
}