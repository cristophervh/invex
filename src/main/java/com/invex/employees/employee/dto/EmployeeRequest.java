package com.invex.employees.employee.dto;

import static com.invex.employees.employee.EmployeeConstants.DATE_PATTERN;
import static com.invex.employees.employee.EmployeeConstants.MAX_AGE;
import static com.invex.employees.employee.EmployeeConstants.MIN_AGE;
import static com.invex.employees.employee.EmployeeConstants.NAME_MAX_LENGTH;
import static com.invex.employees.employee.EmployeeConstants.POSITION_MAX_LENGTH;

import java.time.LocalDate;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.invex.employees.employee.Gender;

public record EmployeeRequest(
        @NotBlank @Size(max = NAME_MAX_LENGTH) String firstName,
        @Size(max = NAME_MAX_LENGTH) String middleName,
        @NotBlank @Size(max = NAME_MAX_LENGTH) String lastName,
        @Size(max = NAME_MAX_LENGTH) String secondLastName,
        @NotNull @Min(MIN_AGE) @Max(MAX_AGE) Integer age,
        @NotNull Gender gender,
        @NotNull @Past @JsonFormat(pattern = DATE_PATTERN) LocalDate birthDate,
        @NotBlank @Size(max = POSITION_MAX_LENGTH) String position,
        Boolean active) {
}
