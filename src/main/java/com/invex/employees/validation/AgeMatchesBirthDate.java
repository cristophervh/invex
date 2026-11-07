package com.invex.employees.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AgeMatchesBirthDateValidator.class)
public @interface AgeMatchesBirthDate {

    String message() default "does not match birthDate";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
