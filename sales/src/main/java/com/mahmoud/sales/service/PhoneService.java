package com.mahmoud.sales.service;

import com.mahmoud.sales.entity.Employee;
import com.mahmoud.sales.entity.Person;
import com.mahmoud.sales.entity.Phone;
import com.mahmoud.sales.repository.PhoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PhoneService {

    @Autowired
    private PhoneRepository phoneRepository;
    @Autowired
    private EmployeeService EmployeeService;
    @Autowired
    private PersonService PersonService;

    // Retrieve all phone records
    public List<Phone> findAllPhones() {
        return phoneRepository.findAll();
    }

    // Retrieve a phone by its ID
    public Optional<Phone> findPhoneById(Integer id) {
        return phoneRepository.findById(id);
    }

    // Retrieve phones by employee ID
    public List<Phone> findPhonesByEmployeeId(Integer employeeId) {

        Employee employee = EmployeeService.findEmployeeById(employeeId).orElse(null);
        if (employee != null) {
            return phoneRepository.findByEmployee(employee);
        }
        return new ArrayList<>();
    }

    // Retrieve phones by person ID
    public List<Phone> findPhonesByPersonId(Integer personId) {
        Person person = PersonService.findPersonById(personId).orElse(null);
        if (person != null) {
            return phoneRepository.findByPerson(person);
        }
        return new ArrayList<>();
    }

    public Phone savePhone(Phone phone) {
        return phoneRepository.save(phone);
    }

    public void deletePhone(Integer id) {
        phoneRepository.deleteById(id);
    }
}
