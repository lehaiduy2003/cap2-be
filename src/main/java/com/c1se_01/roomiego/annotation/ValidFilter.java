package com.c1se_01.roomiego.annotation;
import com.c1se_01.roomiego.validator.FilterValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FilterValidator.class)
public @interface ValidFilter {
    String message() default "Invalid filter format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}