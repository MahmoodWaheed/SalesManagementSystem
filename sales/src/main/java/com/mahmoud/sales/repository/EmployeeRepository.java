package com.mahmoud.sales.repository;

import com.mahmoud.sales.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    Employee findByEmail(String email);

    List<Employee> findByRole(String role);

    @Query("""
            SELECT e FROM Employee e
            WHERE (
                    :q IS NULL OR :q = ''
                    OR LOWER(e.name) LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(e.role) LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(e.email) LIKE LOWER(CONCAT('%', :q, '%'))
                  )
            """)
    Page<Employee> search(@Param("q") String q, Pageable pageable);
}
