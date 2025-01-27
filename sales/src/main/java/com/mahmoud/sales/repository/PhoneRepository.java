package com.mahmoud.sales.repository;

import com.mahmoud.sales.entity.Employee;
import com.mahmoud.sales.entity.Person;
import com.mahmoud.sales.entity.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhoneRepository extends JpaRepository<Phone, Integer> {
    // Additional query methods to fetch phones based on employee or person (if needed)
    List<Phone> findByEmployee(Employee employee);
    List<Phone> findByPerson(Person person);
}
