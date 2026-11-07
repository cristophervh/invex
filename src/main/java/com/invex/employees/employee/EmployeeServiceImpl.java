package com.invex.employees.employee;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.invex.employees.employee.dto.EmployeeRequest;
import com.invex.employees.employee.dto.EmployeeResponse;
import com.invex.employees.employee.dto.EmployeeUpdateRequest;
import com.invex.employees.exception.DuplicateEmployeeException;
import com.invex.employees.exception.EmployeeNotFoundException;
import com.invex.employees.exception.InvalidEmployeeDataException;

@Service
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);
    private static final DateTimeFormatter BIRTH_DATE_FORMAT =
            DateTimeFormatter.ofPattern(EmployeeConstants.DATE_PATTERN);

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final AgeCalculator ageCalculator;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper,
                               AgeCalculator ageCalculator) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
        this.ageCalculator = ageCalculator;
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

        Set<String> keysInRequest = new HashSet<>();
        for (Employee employee : employees) {
            if (!keysInRequest.add(identityKey(employee))) {
                throw new DuplicateEmployeeException(describe(employee) + " appears more than once in the request");
            }
            ensureNotRegistered(employee, null);
        }

        List<EmployeeResponse> created = employeeRepository.saveAll(employees).stream()
                .map(employeeMapper::toResponse)
                .toList();

        log.info("Created {} employee(s)", created.size());
        return created;
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, EmployeeUpdateRequest request) {
        if (request.hasNoChanges()) {
            throw new InvalidEmployeeDataException("At least one field must be provided");
        }
        Employee employee = getEmployee(id);
        employeeMapper.updateEntity(employee, request);

        // The request may change only one of the two, so the check runs on the merged result
        if (!ageCalculator.matches(employee.getAge(), employee.getBirthDate())) {
            throw new InvalidEmployeeDataException("age " + employee.getAge() + " does not match birthDate, expected "
                    + ageCalculator.ageOf(employee.getBirthDate()));
        }
        ensureNotRegistered(employee, id);

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
        return employeeRepository.searchByName(escapeLike(name.trim())).stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    private Employee getEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    private void ensureNotRegistered(Employee employee, Long currentId) {
        boolean exists = employeeRepository
                .findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                        employee.getFirstName(), employee.getLastName(), employee.getBirthDate())
                .stream()
                .filter(existing -> !existing.getId().equals(currentId))
                .anyMatch(existing -> sameText(existing.getSecondLastName(), employee.getSecondLastName()));
        if (exists) {
            throw new DuplicateEmployeeException(describe(employee) + " is already registered");
        }
    }

    // Same person = same first name, both last names and birth date, ignoring case
    private String identityKey(Employee employee) {
        return String.join("|",
                employee.getFirstName().toLowerCase(Locale.ROOT),
                employee.getLastName().toLowerCase(Locale.ROOT),
                Objects.toString(employee.getSecondLastName(), "").toLowerCase(Locale.ROOT),
                employee.getBirthDate().toString());
    }

    private boolean sameText(String a, String b) {
        return Objects.toString(a, "").equalsIgnoreCase(Objects.toString(b, ""));
    }

    private String describe(Employee employee) {
        return "Employee " + employee.getFirstName() + " " + employee.getLastName() + " born on "
                + employee.getBirthDate().format(BIRTH_DATE_FORMAT);
    }

    private String escapeLike(String value) {
        return value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
