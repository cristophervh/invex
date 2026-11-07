package com.invex.employees.employee;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // The name must already be escaped with '!' (see EmployeeServiceImpl#escapeLike)
    @Query("""
            select e from Employee e
            where lower(e.firstName) like lower(concat('%', :name, '%')) escape '!'
               or lower(e.middleName) like lower(concat('%', :name, '%')) escape '!'
               or lower(e.lastName) like lower(concat('%', :name, '%')) escape '!'
               or lower(e.secondLastName) like lower(concat('%', :name, '%')) escape '!'
            order by e.id
            """)
    List<Employee> searchByName(@Param("name") String name);

    List<Employee> findByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(String firstName, String lastName,
                                                                             LocalDate birthDate);
}
