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
import com.invex.employees.exception.ApiError;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(BASE_PATH)
@Validated
@Tag(name = "Employees")
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Role not allowed", content = @Content(schema = @Schema(implementation = ApiError.class)))
})
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(summary = "List all employees")
    @GetMapping
    public List<EmployeeResponse> findAll() {
        return employeeService.findAll();
    }

    @Operation(summary = "Get an employee by id")
    @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{id}")
    public EmployeeResponse findById(@PathVariable @Positive Long id) {
        return employeeService.findById(id);
    }

    // Accepts a single employee or an array (see spring.jackson.deserialization in application.yml)
    @Operation(summary = "Create one or several employees",
            description = "Send a single object or an array of up to 100. The whole request is rejected if any item is invalid or duplicated.")
    @ApiResponse(responseCode = "201", description = "Employees created")
    @ApiResponse(responseCode = "409", description = "Employee already registered", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping
    public ResponseEntity<List<EmployeeResponse>> create(
            @RequestBody @NotEmpty @Size(max = MAX_BATCH_SIZE) List<@NotNull @Valid EmployeeRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(requests));
    }

    @Operation(summary = "Update some or all fields of an employee",
            description = "Fields that are not sent keep their current value. An empty string clears middleName or secondLastName.")
    @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Duplicate or concurrent update", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable @Positive Long id,
                                   @RequestBody @Valid EmployeeUpdateRequest request) {
        return employeeService.update(id, request);
    }

    @Operation(summary = "Delete an employee")
    @ApiResponse(responseCode = "204", description = "Employee deleted")
    @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        employeeService.delete(id);
    }

    @Operation(summary = "Search employees by name",
            description = "Case-insensitive partial match on first name, middle name and both last names.")
    @GetMapping("/search")
    public List<EmployeeResponse> search(
            @RequestParam @NotBlank @Size(min = SEARCH_MIN_LENGTH, max = NAME_MAX_LENGTH) String name) {
        return employeeService.searchByName(name);
    }
}
