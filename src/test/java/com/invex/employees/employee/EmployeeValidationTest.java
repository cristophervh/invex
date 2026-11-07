package com.invex.employees.employee;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.invex.employees.exception.DuplicateEmployeeException;
import com.invex.employees.exception.InvalidEmployeeDataException;

/**
 * Input validation rules, checked through the HTTP layer so the error body is verified too.
 * The clock is fixed on 6 November 2026.
 */
@WebMvcTest(EmployeeController.class)
@Import(AgeCalculator.class)
class EmployeeValidationTest {

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-11-06T12:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void shouldAcceptNamesWithAccentsApostrophesAndHyphens() throws Exception {
        when(employeeService.create(anyList())).thenReturn(List.of());

        postEmployee(employee("José-Luis", "O'Connor", "Núñez", 36, "MALE", "15-05-1990", "Backend Developer"))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ana1", "Ana@", "  ", "Ana  María", "-Ana"})
    void shouldRejectInvalidFirstNames(String firstName) throws Exception {
        postEmployee(employee(firstName, "García", null, 36, "FEMALE", "15-05-1990", "QA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[?(@.field == '[0].firstName')]").exists());
    }

    @Test
    void shouldRejectNamesLongerThan50Characters() throws Exception {
        postEmployee(employee("A".repeat(51), "García", null, 36, "FEMALE", "15-05-1990", "QA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("[0].firstName"));
    }

    @Test
    void shouldRejectMissingRequiredFields() throws Exception {
        postEmployee("{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[?(@.field == '[0].firstName')]").exists())
                .andExpect(jsonPath("$.validationErrors[?(@.field == '[0].lastName')]").exists())
                .andExpect(jsonPath("$.validationErrors[?(@.field == '[0].age')]").exists())
                .andExpect(jsonPath("$.validationErrors[?(@.field == '[0].gender')]").exists())
                .andExpect(jsonPath("$.validationErrors[?(@.field == '[0].birthDate')]").exists())
                .andExpect(jsonPath("$.validationErrors[?(@.field == '[0].position')]").exists());
    }

    @ParameterizedTest
    @ValueSource(strings = {"31-02-2000", "29-02-2001", "32-01-1990", "15-13-1990"})
    void shouldRejectImpossibleDates(String birthDate) throws Exception {
        postEmployee(employee("Ana", "García", null, 36, "FEMALE", birthDate, "QA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("[0].birthDate"))
                .andExpect(jsonPath("$.validationErrors[0].message", containsString("dd-MM-yyyy")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1990-05-15", "15/05/1990", "15-5-1990", "abc"})
    void shouldRejectDatesInAnotherFormat(String birthDate) throws Exception {
        postEmployee(employee("Ana", "García", null, 36, "FEMALE", birthDate, "QA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("[0].birthDate"));
    }

    @Test
    void shouldAcceptLeapDayOnLeapYear() throws Exception {
        when(employeeService.create(anyList())).thenReturn(List.of());

        postEmployee(employee("Ana", "García", null, 26, "FEMALE", "29-02-2000", "QA"))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectFutureBirthDate() throws Exception {
        postEmployee(employee("Ana", "García", null, 18, "FEMALE", "01-01-2030", "QA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[?(@.field == '[0].birthDate')]").exists());
    }

    @ParameterizedTest
    @ValueSource(ints = {17, 101, -1})
    void shouldRejectAgeOutOfRange(int age) throws Exception {
        postEmployee(employee("Ana", "García", null, age, "FEMALE", "15-05-1990", "QA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[?(@.field == '[0].age')]").exists());
    }

    @Test
    void shouldRejectAgeThatDoesNotMatchBirthDate() throws Exception {
        postEmployee(employee("Ana", "García", null, 30, "FEMALE", "15-05-1990", "QA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("[0].age"))
                .andExpect(jsonPath("$.validationErrors[0].message").value("does not match birthDate, expected 36"));
    }

    @Test
    void shouldRejectUnknownGenderListingAllowedValues() throws Exception {
        postEmployee(employee("Ana", "García", null, 36, "X", "15-05-1990", "QA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("[0].gender"))
                .andExpect(jsonPath("$.validationErrors[0].message", containsString("MALE, FEMALE, OTHER")));
    }

    @Test
    void shouldAcceptGenderInLowerCase() throws Exception {
        when(employeeService.create(anyList())).thenReturn(List.of());

        postEmployee(employee("Ana", "García", null, 36, "female", "15-05-1990", "QA"))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectInvalidPosition() throws Exception {
        postEmployee(employee("Ana", "García", null, 36, "FEMALE", "15-05-1990", "<script>"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[?(@.field == '[0].position')]").exists());
    }

    @Test
    void shouldRejectBatchLargerThanLimit() throws Exception {
        String one = employee("Ana", "García", null, 36, "FEMALE", "15-05-1990", "QA");
        String batch = "[" + String.join(",", Collections.nCopies(101, one)) + "]";

        postEmployee(batch)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("requests"));
    }

    @Test
    void shouldReturn409WhenEmployeeIsDuplicated() throws Exception {
        when(employeeService.create(anyList()))
                .thenThrow(new DuplicateEmployeeException("Employee Ana García born on 15-05-1990 is already registered"));

        postEmployee(employee("Ana", "García", null, 36, "FEMALE", "15-05-1990", "QA"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already registered")));
    }

    @Test
    void shouldRejectInvalidFieldOnPartialUpdate() throws Exception {
        mockMvc.perform(put("/employees/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\": \"Ana2\", \"birthDate\": \"31-02-2000\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenUpdateHasNoFields() throws Exception {
        when(employeeService.update(eq(1L), any()))
                .thenThrow(new InvalidEmployeeDataException("At least one field must be provided"));

        mockMvc.perform(put("/employees/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("At least one field must be provided"));
    }

    @Test
    void shouldReturn400WhenUpdateBodyIsMissing() throws Exception {
        mockMvc.perform(put("/employees/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectSearchWithOneCharacter() throws Exception {
        mockMvc.perform(get("/employees/search").param("name", "a"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("name"));
    }

    @Test
    void shouldRejectNonPositiveId() throws Exception {
        mockMvc.perform(get("/employees/0"))
                .andExpect(status().isBadRequest());
    }

    private ResultActions postEmployee(String body) throws Exception {
        return mockMvc.perform(post("/employees").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private String employee(String firstName, String lastName, String secondLastName, int age, String gender,
                            String birthDate, String position) {
        return """
                {"firstName": %s, "lastName": %s, "secondLastName": %s, "age": %d, "gender": "%s",
                 "birthDate": "%s", "position": "%s"}
                """.formatted(quote(firstName), quote(lastName), quote(secondLastName), age, gender, birthDate,
                position);
    }

    private String quote(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }
}
