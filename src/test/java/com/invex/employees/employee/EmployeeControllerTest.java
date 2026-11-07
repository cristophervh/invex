package com.invex.employees.employee;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.invex.employees.employee.dto.EmployeeResponse;
import com.invex.employees.exception.EmployeeNotFoundException;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    private static final String VALID_EMPLOYEE = """
            {
              "firstName": "Ana",
              "lastName": "García",
              "age": 35,
              "gender": "FEMALE",
              "birthDate": "15-05-1990",
              "position": "Backend Developer"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void shouldReturnEmployeeById() throws Exception {
        when(employeeService.findById(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.birthDate").value("15-05-1990"));
    }

    @Test
    void shouldReturn404WhenEmployeeDoesNotExist() throws Exception {
        when(employeeService.findById(99L)).thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(get("/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/employees/99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn400WhenIdIsNotANumber() throws Exception {
        mockMvc.perform(get("/employees/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateSingleEmployee() throws Exception {
        when(employeeService.create(anyList())).thenReturn(List.of(buildResponse()));

        mockMvc.perform(post("/employees").contentType(MediaType.APPLICATION_JSON).content(VALID_EMPLOYEE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void shouldCreateSeveralEmployees() throws Exception {
        when(employeeService.create(anyList())).thenReturn(List.of(buildResponse(), buildResponse()));

        mockMvc.perform(post("/employees").contentType(MediaType.APPLICATION_JSON)
                        .content("[" + VALID_EMPLOYEE + "," + VALID_EMPLOYEE + "]"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldReturn400WithFieldErrorsWhenEmployeeIsInvalid() throws Exception {
        String invalid = """
                [{"firstName": "", "lastName": "García", "age": 10, "gender": "FEMALE",
                  "birthDate": "15-05-1990", "position": "QA"}, null]
                """;

        mockMvc.perform(post("/employees").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors[?(@.field == '[0].firstName')]").exists())
                .andExpect(jsonPath("$.validationErrors[?(@.field == '[0].age')]").exists())
                .andExpect(jsonPath("$.validationErrors[?(@.field == '[1]')]").exists());
    }

    @Test
    void shouldReturn400WhenListIsEmpty() throws Exception {
        mockMvc.perform(post("/employees").contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/employees").contentType(MediaType.APPLICATION_JSON).content("{\"firstName\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or malformed"));
    }

    @Test
    void shouldUpdateEmployee() throws Exception {
        when(employeeService.update(eq(1L), any())).thenReturn(buildResponse());

        mockMvc.perform(put("/employees/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"position\": \"Backend Developer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("Backend Developer"));
    }

    @Test
    void shouldDeleteEmployee() throws Exception {
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingMissingEmployee() throws Exception {
        doThrow(new EmployeeNotFoundException(7L)).when(employeeService).delete(7L);

        mockMvc.perform(delete("/employees/7"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldSearchByName() throws Exception {
        when(employeeService.searchByName("an")).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/employees/search").param("name", "an"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldReturn400WhenSearchNameIsBlank() throws Exception {
        mockMvc.perform(get("/employees/search").param("name", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("name"));
    }

    private EmployeeResponse buildResponse() {
        return new EmployeeResponse(1L, "Ana", null, "García", null, 35, Gender.FEMALE,
                LocalDate.of(1990, 5, 15), "Backend Developer", Instant.now(), true);
    }
}
