package com.mahmoud.sales.service;

import com.mahmoud.sales.entity.Employee;
import com.mahmoud.sales.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> findAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> findEmployeeById(Integer id) {
        return employeeRepository.findById(id);
    }

    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public void deleteEmployee(Integer id) {
        employeeRepository.deleteById(id);
    }

    public List<Employee> findByRole(String role) {
        return employeeRepository.findByRole(role);
    }

    /**
     * Server-side paging + sorting + search.
     * sortField allow-list: name, role, email, salary
     */
    public Page<Employee> findEmployeesPaged(String search, int pageIndex, int pageSize, String sortField, boolean asc) {
        int safeIndex = Math.max(pageIndex, 0);
        int safeSize = Math.max(pageSize, 1);

        String field = (sortField == null || sortField.isBlank()) ? "name" : sortField.trim();
        if (!field.equals("name") && !field.equals("role") && !field.equals("email") && !field.equals("salary")) {
            field = "name";
        }

        Sort sort = Sort.by(asc ? Sort.Direction.ASC : Sort.Direction.DESC, field);
        Pageable pageable = PageRequest.of(safeIndex, safeSize, sort);

        String q = (search == null) ? "" : search.trim();
        if (q.isBlank()) {
            return employeeRepository.findAll(pageable);
        }

        return employeeRepository.search(q, pageable);
    }
}