package com.invex.employees.employee.dto;

import static com.invex.employees.employee.EmployeeConstants.MAX_AGE;
import static com.invex.employees.employee.EmployeeConstants.MIN_AGE;
import static com.invex.employees.employee.EmployeeConstants.NAME_MAX_LENGTH;
import static com.invex.employees.employee.EmployeeConstants.NAME_MESSAGE;
import static com.invex.employees.employee.EmployeeConstants.NAME_REGEX;
import static com.invex.employees.employee.EmployeeConstants.OPTIONAL_NAME_REGEX;
import static com.invex.employees.employee.EmployeeConstants.POSITION_MAX_LENGTH;
import static com.invex.employees.employee.EmployeeConstants.POSITION_MESSAGE;
import static com.invex.employees.employee.EmployeeConstants.POSITION_REGEX;

import java.time.LocalDate;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.invex.employees.employee.Gender;
import com.invex.employees.validation.AgeMatchesBirthDate;
import com.invex.employees.validation.StrictLocalDateDeserializer;

@AgeMatchesBirthDate
public record EmployeeRequest(
        @NotBlank @Size(max = NAME_MAX_LENGTH) @Pattern(regexp = NAME_REGEX, message = NAME_MESSAGE)
        String firstName,

        @Size(max = NAME_MAX_LENGTH) @Pattern(regexp = OPTIONAL_NAME_REGEX, message = NAME_MESSAGE)
        String middleName,

        @NotBlank @Size(max = NAME_MAX_LENGTH) @Pattern(regexp = NAME_REGEX, message = NAME_MESSAGE)
        String lastName,

        @Size(max = NAME_MAX_LENGTH) @Pattern(regexp = OPTIONAL_NAME_REGEX, message = NAME_MESSAGE)
        String secondLastName,

        @NotNull @Min(MIN_AGE) @Max(MAX_AGE)
        Integer age,

        @NotNull
        Gender gender,

        @NotNull @Past @JsonDeserialize(using = StrictLocalDateDeserializer.class)
        LocalDate birthDate,

        @NotBlank @Size(max = POSITION_MAX_LENGTH) @Pattern(regexp = POSITION_REGEX, message = POSITION_MESSAGE)
        String position,

        Boolean active) {
}
