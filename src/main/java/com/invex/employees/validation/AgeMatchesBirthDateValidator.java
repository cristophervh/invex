package com.invex.employees.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.invex.employees.employee.AgeCalculator;
import com.invex.employees.employee.dto.EmployeeRequest;

public class AgeMatchesBirthDateValidator implements ConstraintValidator<AgeMatchesBirthDate, EmployeeRequest> {

    private final AgeCalculator ageCalculator;

    public AgeMatchesBirthDateValidator(AgeCalculator ageCalculator) {
        this.ageCalculator = ageCalculator;
    }

    @Override
    public boolean isValid(EmployeeRequest request, ConstraintValidatorContext context) {
        if (request == null || ageCalculator.matches(request.age(), request.birthDate())) {
            return true;
        }
        int expected = ageCalculator.ageOf(request.birthDate());
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("does not match birthDate, expected " + expected)
                .addPropertyNode("age")
                .addConstraintViolation();
        return false;
    }
}
