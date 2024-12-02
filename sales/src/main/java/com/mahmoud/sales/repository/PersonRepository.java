package com.mahmoud.sales.repository;

import com.mahmoud.sales.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Integer> {
    // Custom query methods can be defined here if needed
}
