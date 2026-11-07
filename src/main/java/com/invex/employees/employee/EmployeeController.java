package com.invex.employees.employee;

import static com.invex.employees.employee.EmployeeConstants.BASE_PATH;
import static com.invex.employees.employee.EmployeeConstants.MAX_BATCH_SIZE;
import static com.invex.employees.employee.EmployeeConstants.NAME_MAX_LENGTH;
import static com.invex.employees.employee.EmployeeConstants.SEARCH_MIN_LENGTH;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.invex.employees.employee.dto.EmployeeRequest;
import com.invex.employees.employee.dto.EmployeeResponse;
import com.invex.employees.employee.dto.EmployeeUpdateRequest;

@RestController
@RequestMapping(BASE_PATH)
@Validated
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<EmployeeResponse> findAll() {
        return employeeService.findAll();
    }

    @GetMapping("/{id}")
    public EmployeeResponse findById(@PathVariable @Positive Long id) {
        return employeeService.findById(id);
    }

    // Accepts a single employee or an array (see spring.jackson.deserialization in application.yml)
    @PostMapping
    public ResponseEntity<List<EmployeeResponse>> create(
            @RequestBody @NotEmpty @Size(max = MAX_BATCH_SIZE) List<@NotNull @Valid EmployeeRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(requests));
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable @Positive Long id,
                                   @RequestBody @Valid EmployeeUpdateRequest request) {
        return employeeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        employeeService.delete(id);
    }

    @GetMapping("/search")
    public List<EmployeeResponse> search(
            @RequestParam @NotBlank @Size(min = SEARCH_MIN_LENGTH, max = NAME_MAX_LENGTH) String name) {
        return employeeService.searchByName(name);
    }
}
