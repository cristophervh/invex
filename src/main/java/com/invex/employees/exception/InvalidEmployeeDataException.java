package com.invex.employees.exception;

public class InvalidEmployeeDataException extends RuntimeException {

    public InvalidEmployeeDataException(String message) {
        super(message);
    }
}
