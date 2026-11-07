package com.invex.employees.employee;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;

import org.springframework.stereotype.Component;

@Component
public class AgeCalculator {

    private final Clock clock;

    public AgeCalculator(Clock clock) {
        this.clock = clock;
    }

    public int ageOf(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now(clock)).getYears();
    }

    public boolean matches(Integer age, LocalDate birthDate) {
        if (age == null || birthDate == null) {
            return true;
        }
        return ageOf(birthDate) == age;
    }
}
