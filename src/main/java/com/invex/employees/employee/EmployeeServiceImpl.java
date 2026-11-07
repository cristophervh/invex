package com.invex.employees.employee;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.invex.employees.employee.dto.EmployeeRequest;
import com.invex.employees.employee.dto.EmployeeResponse;
import com.invex.employees.employee.dto.EmployeeUpdateRequest;
import com.invex.employees.exception.EmployeeNotFoundException;

@Service
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
    }

    @Override
    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll().stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    @Override
    public EmployeeResponse findById(Long id) {
        return employeeMapper.toResponse(getEmployee(id));
    }

    @Override
    @Transactional
    public List<EmployeeResponse> create(List<EmployeeRequest> requests) {
        List<Employee> employees = requests.stream()
                .map(employeeMapper::toEntity)
                .toList();

        List<EmployeeResponse> created = employeeRepository.saveAll(employees).stream()
                .map(employeeMapper::toResponse)
                .toList();

        log.info("Created {} employee(s)", created.size());
        return created;
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, EmployeeUpdateRequest request) {
        Employee employee = getEmployee(id);
        employeeMapper.updateEntity(employee, request);
        Employee saved = employeeRepository.saveAndFlush(employee);

        log.info("Updated employee {}", id);
        return employeeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Employee employee = getEmployee(id);
        employeeRepository.delete(employee);
        log.info("Deleted employee {}", id);
    }

    @Override
    public List<EmployeeResponse> searchByName(String name) {
        return employeeRepository.searchByName(name.trim()).stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    private Employee getEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
    }
}
