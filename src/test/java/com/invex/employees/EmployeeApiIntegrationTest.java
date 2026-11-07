package com.invex.employees;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.Period;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.invex.employees.employee.EmployeeRepository;

/**
 * Full stack: security filter chain, controllers, service, JPA and H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void cleanDatabase() {
        employeeRepository.deleteAll();
    }

    @Test
    void shouldRunTheWholeCrudFlowWithAnAdminToken() throws Exception {
        String admin = token("admin", "admin-test");

        String created = mockMvc.perform(post("/employees").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[" + employeeJson("Ana", "García", 1990) + "," + employeeJson("Luis", "Pérez", 1985) + "]"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get(0).get("id").asLong();

        mockMvc.perform(get("/employees").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/employees/search").param("name", "garc").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id));

        mockMvc.perform(put("/employees/" + id).header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"position\": \"Tech Lead\", \"active\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("Tech Lead"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.firstName").value("Ana"));

        mockMvc.perform(post("/employees").header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("ana", "GARCÍA", 1990)))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/employees/" + id).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/employees/" + id).header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectRequestsWithoutToken() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/employees"));
    }

    @Test
    void shouldRejectInvalidToken() throws Exception {
        mockMvc.perform(get("/employees").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLetReaderListButNotCreate() throws Exception {
        String reader = token("reader", "reader-test");

        mockMvc.perform(get("/employees").header(HttpHeaders.AUTHORIZATION, reader))
                .andExpect(status().isOk());

        mockMvc.perform(post("/employees").header(HttpHeaders.AUTHORIZATION, reader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("Ana", "García", 1990)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void shouldRejectWrongPassword() throws Exception {
        mockMvc.perform(post("/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"admin\", \"password\": \"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void shouldReturnRequestIdHeader() throws Exception {
        mockMvc.perform(get("/employees").header("X-Request-Id", "abc-123"))
                .andExpect(header().string("X-Request-Id", "abc-123"));

        mockMvc.perform(get("/employees"))
                .andExpect(header().string("X-Request-Id", notNullValue()));
    }

    private String token(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(body);
        return "Bearer " + json.get("accessToken").asText();
    }

    // Age is computed from today's date so the test keeps passing as time goes by
    private String employeeJson(String firstName, String lastName, int birthYear) {
        LocalDate birthDate = LocalDate.of(birthYear, 1, 15);
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        return """
                {"firstName": "%s", "lastName": "%s", "age": %d, "gender": "FEMALE",
                 "birthDate": "15-01-%d", "position": "Backend Developer"}
                """.formatted(firstName, lastName, age, birthYear);
    }
}
