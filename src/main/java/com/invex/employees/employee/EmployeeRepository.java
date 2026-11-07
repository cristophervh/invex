package com.invex.employees.employee;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("""
            select e from Employee e
            where lower(e.firstName) like lower(concat('%', :name, '%'))
               or lower(e.middleName) like lower(concat('%', :name, '%'))
               or lower(e.lastName) like lower(concat('%', :name, '%'))
               or lower(e.secondLastName) like lower(concat('%', :name, '%'))
            order by e.id
            """)
    List<Employee> searchByName(@Param("name") String name);
}
