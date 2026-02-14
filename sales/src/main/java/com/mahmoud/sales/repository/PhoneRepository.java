package com.mahmoud.sales.repository;

import com.mahmoud.sales.entity.Employee;
import com.mahmoud.sales.entity.Person;
import com.mahmoud.sales.entity.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhoneRepository extends JpaRepository<Phone, Integer> {

    List<Phone> findByEmployee(Employee employee);

    List<Phone> findByPerson(Person person);

    // Fast lookups without fetching Person entity first
    List<Phone> findByPersonId(Integer personId);

    // Batch loading for a page
    List<Phone> findByPersonIdIn(List<Integer> personIds);
}
