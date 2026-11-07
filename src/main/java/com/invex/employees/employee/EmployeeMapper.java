package com.invex.employees.employee;

import org.springframework.stereotype.Component;

import com.invex.employees.employee.dto.EmployeeRequest;
import com.invex.employees.employee.dto.EmployeeResponse;
import com.invex.employees.employee.dto.EmployeeUpdateRequest;

@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeRequest request) {
        Employee employee = new Employee();
        employee.setFirstName(request.firstName().trim());
        employee.setMiddleName(trimToNull(request.middleName()));
        employee.setLastName(request.lastName().trim());
        employee.setSecondLastName(trimToNull(request.secondLastName()));
        employee.setAge(request.age());
        employee.setGender(request.gender());
        employee.setBirthDate(request.birthDate());
        employee.setPosition(request.position().trim());
        employee.setActive(request.active() == null || request.active());
        return employee;
    }

    public EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getMiddleName(),
                employee.getLastName(),
                employee.getSecondLastName(),
                employee.getAge(),
                employee.getGender(),
                employee.getBirthDate(),
                employee.getPosition(),
                employee.getCreatedAt(),
                employee.isActive());
    }

    public void updateEntity(Employee employee, EmployeeUpdateRequest request) {
        if (request.firstName() != null) {
            employee.setFirstName(request.firstName().trim());
        }
        if (request.middleName() != null) {
            employee.setMiddleName(trimToNull(request.middleName()));
        }
        if (request.lastName() != null) {
            employee.setLastName(request.lastName().trim());
        }
        if (request.secondLastName() != null) {
            employee.setSecondLastName(trimToNull(request.secondLastName()));
        }
        if (request.age() != null) {
            employee.setAge(request.age());
        }
        if (request.gender() != null) {
            employee.setGender(request.gender());
        }
        if (request.birthDate() != null) {
            employee.setBirthDate(request.birthDate());
        }
        if (request.position() != null) {
            employee.setPosition(request.position().trim());
        }
        if (request.active() != null) {
            employee.setActive(request.active());
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
