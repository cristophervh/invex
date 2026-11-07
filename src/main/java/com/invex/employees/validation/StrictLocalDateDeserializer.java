package com.invex.employees.validation;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Parses dd-MM-yyyy rejecting impossible dates. Jackson's default resolver silently
 * turns 31-02-2000 into 29-02-2000, which would store a date the client never sent.
 */
public class StrictLocalDateDeserializer extends StdDeserializer<LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    public StrictLocalDateDeserializer() {
        super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getValueAsString();
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), FORMATTER);
        } catch (DateTimeParseException ex) {
            return (LocalDate) context.handleWeirdStringValue(LocalDate.class, value,
                    "expected format dd-MM-yyyy");
        }
    }
}
