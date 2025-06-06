package com.mahmoud.sales.repository;

import com.mahmoud.sales.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    // Additional custom query methods can be defined here if needed
    public Employee findByEmail(String email);
    public List<Employee> findByRole(String role);
}
