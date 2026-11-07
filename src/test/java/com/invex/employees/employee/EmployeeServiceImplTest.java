package com.invex.employees.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.invex.employees.employee.dto.EmployeeRequest;
import com.invex.employees.employee.dto.EmployeeResponse;
import com.invex.employees.employee.dto.EmployeeUpdateRequest;
import com.invex.employees.exception.EmployeeNotFoundException;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeServiceImpl(employeeRepository, new EmployeeMapper());
    }

    @Test
    void shouldReturnAllEmployees() {
        when(employeeRepository.findAll()).thenReturn(List.of(buildEmployee("Ana"), buildEmployee("Luis")));

        List<EmployeeResponse> result = employeeService.findAll();

        assertThat(result).extracting(EmployeeResponse::firstName).containsExactly("Ana", "Luis");
    }

    @Test
    void shouldThrowNotFoundWhenEmployeeDoesNotExist() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.findById(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldCreateSeveralEmployeesInOneCall() {
        when(employeeRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<EmployeeResponse> result = employeeService.create(List.of(
                buildRequest("Ana", null),
                buildRequest("  Luis  ", false)));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).active()).isTrue();
        assertThat(result.get(1).firstName()).isEqualTo("Luis");
        assertThat(result.get(1).active()).isFalse();
    }

    @Test
    void shouldUpdateOnlyTheFieldsSent() {
        Employee employee = buildEmployee("Ana");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.saveAndFlush(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeUpdateRequest request = new EmployeeUpdateRequest(
                null, null, null, null, null, null, null, "Tech Lead", null);

        EmployeeResponse result = employeeService.update(1L, request);

        assertThat(result.position()).isEqualTo("Tech Lead");
        assertThat(result.firstName()).isEqualTo("Ana");
        assertThat(result.lastName()).isEqualTo("García");
        assertThat(result.active()).isTrue();
    }

    @Test
    void shouldNotUpdateWhenEmployeeDoesNotExist() {
        when(employeeRepository.findById(5L)).thenReturn(Optional.empty());
        EmployeeUpdateRequest request = new EmployeeUpdateRequest(
                null, null, null, null, null, null, null, "Tech Lead", null);

        assertThatThrownBy(() -> employeeService.update(5L, request))
                .isInstanceOf(EmployeeNotFoundException.class);
        verify(employeeRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldDeleteExistingEmployee() {
        Employee employee = buildEmployee("Ana");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        employeeService.delete(1L);

        verify(employeeRepository).delete(employee);
    }

    @Test
    void shouldTrimNameBeforeSearching() {
        when(employeeRepository.searchByName("gar")).thenReturn(List.of(buildEmployee("Ana")));

        List<EmployeeResponse> result = employeeService.searchByName("  gar ");

        assertThat(result).hasSize(1);
    }

    private Employee buildEmployee(String firstName) {
        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName("García");
        employee.setAge(35);
        employee.setGender(Gender.FEMALE);
        employee.setBirthDate(LocalDate.of(1990, 5, 15));
        employee.setPosition("Backend Developer");
        return employee;
    }

    private EmployeeRequest buildRequest(String firstName, Boolean active) {
        return new EmployeeRequest(firstName, null, "García", null, 35, Gender.FEMALE,
                LocalDate.of(1990, 5, 15), "Backend Developer", active);
    }
}
