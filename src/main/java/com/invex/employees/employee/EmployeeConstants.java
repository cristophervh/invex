package com.invex.employees.employee;

public final class EmployeeConstants {

    public static final String BASE_PATH = "/employees";
    public static final String DATE_PATTERN = "dd-MM-yyyy";
    public static final String DATE_TIME_PATTERN = "dd-MM-yyyy HH:mm:ss";

    public static final int NAME_MAX_LENGTH = 50;
    public static final int POSITION_MAX_LENGTH = 80;
    public static final int MIN_AGE = 18;
    public static final int MAX_AGE = 100;
    public static final int MAX_BATCH_SIZE = 100;
    public static final int SEARCH_MIN_LENGTH = 2;

    // Letters (accents and ñ included) separated by a single space, apostrophe or hyphen
    public static final String NAME_REGEX = "^\\s*\\p{L}+(?:[ '\\-]\\p{L}+)*\\s*$";
    public static final String OPTIONAL_NAME_REGEX = "^\\s*$|" + NAME_REGEX;
    public static final String POSITION_REGEX = "^\\s*[\\p{L}\\p{N}][\\p{L}\\p{N} .,&/()'\\-]*$";
    public static final String NAME_MESSAGE = "must contain only letters, spaces, apostrophes or hyphens";
    public static final String POSITION_MESSAGE = "contains invalid characters";

    private EmployeeConstants() {
    }
}
