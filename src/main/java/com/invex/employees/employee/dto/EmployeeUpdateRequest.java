package com.invex.employees.employee.dto;

import static com.invex.employees.employee.EmployeeConstants.DATE_PATTERN;
import static com.invex.employees.employee.EmployeeConstants.MAX_AGE;
import static com.invex.employees.employee.EmployeeConstants.MIN_AGE;
import static com.invex.employees.employee.EmployeeConstants.NAME_MAX_LENGTH;
import static com.invex.employees.employee.EmployeeConstants.POSITION_MAX_LENGTH;

import java.time.LocalDate;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.invex.employees.employee.Gender;

/**
 * Every field is optional: a null value means "keep the current value".
 */
public record EmployeeUpdateRequest(
        @Size(min = 1, max = NAME_MAX_LENGTH) String firstName,
        @Size(max = NAME_MAX_LENGTH) String middleName,
        @Size(min = 1, max = NAME_MAX_LENGTH) String lastName,
        @Size(max = NAME_MAX_LENGTH) String secondLastName,
        @Min(MIN_AGE) @Max(MAX_AGE) Integer age,
        Gender gender,
        @Past @JsonFormat(pattern = DATE_PATTERN) LocalDate birthDate,
        @Size(min = 1, max = POSITION_MAX_LENGTH) String position,
        Boolean active) {
}
