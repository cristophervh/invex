package com.invex.employees.exception;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.invex.employees.employee.EmployeeConstants;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String VALIDATION_FAILED = "Validation failed";

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EmployeeNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(DuplicateEmployeeException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateEmployeeException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(InvalidEmployeeDataException.class)
    public ResponseEntity<ApiError> handleInvalidData(InvalidEmployeeDataException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid username or password", request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleInvalidBody(MethodArgumentNotValidException ex,
                                                      HttpServletRequest request) {
        List<ApiError.FieldValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiError.FieldValidationError(error.getField(), error.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, request, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex,
                                                              HttpServletRequest request) {
        List<ApiError.FieldValidationError> errors = ex.getConstraintViolations().stream()
                .map(violation -> new ApiError.FieldValidationError(fieldName(violation), violation.getMessage()))
                .sorted((a, b) -> a.field().compareTo(b.field()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, request, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                         HttpServletRequest request) {
        if (ex.getCause() instanceof InvalidFormatException invalidFormat) {
            String field = fieldPath(invalidFormat);
            String message = "Invalid value '" + invalidFormat.getValue() + "'" + expectedValues(invalidFormat);
            return build(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, request,
                    List.of(new ApiError.FieldValidationError(field, message)));
        }
        return build(HttpStatus.BAD_REQUEST, "Request body is missing or malformed", request, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                       HttpServletRequest request) {
        String message = "Invalid value for parameter '" + ex.getName() + "'";
        return build(HttpStatus.BAD_REQUEST, message, request, List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(MissingServletRequestParameterException ex,
                                                           HttpServletRequest request) {
        String message = "Required parameter '" + ex.getParameterName() + "' is missing";
        return build(HttpStatus.BAD_REQUEST, message, request, List.of());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleConcurrentUpdate(ObjectOptimisticLockingFailureException ex,
                                                           HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "The employee was modified by another request, try again",
                request, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                             HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleMediaType(HttpMediaTypeNotSupportedException ex,
                                                    HttpServletRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type must be application/json",
                request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex,
                                                        HttpServletRequest request) {
        log.warn("Data integrity violation on {} {}", request.getMethod(), request.getRequestURI());
        return build(HttpStatus.CONFLICT, "The request conflicts with existing data", request, List.of());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request,
                                           List<ApiError.FieldValidationError> errors) {
        ApiError body = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(),
                message, request.getRequestURI(), errors);
        return ResponseEntity.status(status).body(body);
    }

    // "create.requests[1].firstName" -> "[1].firstName", "search.name" -> "name"
    private String fieldPath(InvalidFormatException ex) {
        StringBuilder path = new StringBuilder();
        for (JsonMappingException.Reference reference : ex.getPath()) {
            if (reference.getFieldName() != null) {
                path.append(path.length() > 0 ? "." : "").append(reference.getFieldName());
            } else {
                path.append('[').append(reference.getIndex()).append(']');
            }
        }
        return path.toString();
    }

    private String expectedValues(InvalidFormatException ex) {
        Class<?> targetType = ex.getTargetType();
        if (targetType.isEnum()) {
            return ", allowed values: " + Arrays.toString(targetType.getEnumConstants());
        }
        if (LocalDate.class.equals(targetType)) {
            return ", expected a valid date with format " + EmployeeConstants.DATE_PATTERN;
        }
        return "";
    }

    private String fieldName(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString().replace(".<list element>", "");
        int firstDot = path.indexOf('.');
        String field = firstDot >= 0 ? path.substring(firstDot + 1) : path;
        int bracket = field.indexOf('[');
        if (bracket > 0) {
            field = field.substring(bracket);
        }
        return field;
    }
}
