package com.invex.employees.employee;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void shouldSaveEmployee() {
        Employee saved = employeeRepository.saveAndFlush(buildEmployee());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    void shouldFindEmployeeById() {
        Employee saved = employeeRepository.saveAndFlush(buildEmployee());

        Employee found = employeeRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getFirstName()).isEqualTo("Ana");
        assertThat(found.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(found.getGender()).isEqualTo(Gender.FEMALE);
    }

    @Test
    void shouldIncrementVersionOnUpdate() {
        Employee saved = employeeRepository.saveAndFlush(buildEmployee());

        saved.setPosition("Tech Lead");
        Employee updated = employeeRepository.saveAndFlush(saved);

        assertThat(updated.getVersion()).isEqualTo(1L);
    }

    @Test
    void shouldSearchByAnyPartOfTheNameIgnoringCase() {
        employeeRepository.saveAndFlush(buildEmployee());

        List<Employee> byLastName = employeeRepository.searchByName("garc");
        List<Employee> bySecondLastName = employeeRepository.searchByName("LÓPEZ");
        List<Employee> noMatch = employeeRepository.searchByName("xyz");

        assertThat(byLastName).hasSize(1);
        assertThat(bySecondLastName).hasSize(1);
        assertThat(noMatch).isEmpty();
    }

    private Employee buildEmployee() {
        Employee employee = new Employee();
        employee.setFirstName("Ana");
        employee.setLastName("García");
        employee.setSecondLastName("López");
        employee.setAge(35);
        employee.setGender(Gender.FEMALE);
        employee.setBirthDate(LocalDate.of(1990, 5, 15));
        employee.setPosition("Backend Developer");
        return employee;
    }
}
