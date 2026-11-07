package com.invex.employees.employee.dto;

import static com.invex.employees.employee.EmployeeConstants.DATE_PATTERN;
import static com.invex.employees.employee.EmployeeConstants.DATE_TIME_PATTERN;

import java.time.Instant;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.invex.employees.employee.Gender;

public record EmployeeResponse(
        Long id,
        String firstName,
        String middleName,
        String lastName,
        String secondLastName,
        Integer age,
        Gender gender,
        @JsonFormat(pattern = DATE_PATTERN) LocalDate birthDate,
        String position,
        @JsonFormat(pattern = DATE_TIME_PATTERN, timezone = "UTC") Instant createdAt,
        boolean active) {
}
