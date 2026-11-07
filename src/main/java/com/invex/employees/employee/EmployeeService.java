package com.invex.employees.employee;

import java.util.List;

import com.invex.employees.employee.dto.EmployeeRequest;
import com.invex.employees.employee.dto.EmployeeResponse;
import com.invex.employees.employee.dto.EmployeeUpdateRequest;

public interface EmployeeService {

    List<EmployeeResponse> findAll();

    EmployeeResponse findById(Long id);

    List<EmployeeResponse> create(List<EmployeeRequest> requests);

    EmployeeResponse update(Long id, EmployeeUpdateRequest request);

    void delete(Long id);

    List<EmployeeResponse> searchByName(String name);
}
