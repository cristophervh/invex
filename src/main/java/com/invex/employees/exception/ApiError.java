package com.invex.employees.exception;

import static com.invex.employees.employee.EmployeeConstants.DATE_TIME_PATTERN;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
        @JsonFormat(pattern = DATE_TIME_PATTERN, timezone = "UTC") Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldValidationError> validationErrors) {

    public record FieldValidationError(String field, String message) {
    }
}
