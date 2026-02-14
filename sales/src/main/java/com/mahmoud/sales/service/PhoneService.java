package com.mahmoud.sales.service;

import com.mahmoud.sales.entity.Employee;
import com.mahmoud.sales.entity.Phone;
import com.mahmoud.sales.repository.PhoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PhoneService {

    @Autowired
    private PhoneRepository phoneRepository;

    @Autowired
    private EmployeeService EmployeeService;

    public List<Phone> findAllPhones() {
        return phoneRepository.findAll();
    }

    public Optional<Phone> findPhoneById(Integer id) {
        return phoneRepository.findById(id);
    }

    public List<Phone> findPhonesByEmployeeId(Integer employeeId) {
        Employee employee = EmployeeService.findEmployeeById(employeeId).orElse(null);
        if (employee != null) {
            return phoneRepository.findByEmployee(employee);
        }
        return new ArrayList<>();
    }

    public List<Phone> findPhonesByPersonId(Integer personId) {
        if (personId == null) return new ArrayList<>();
        return phoneRepository.findByPersonId(personId);
    }

    /**
     * personId -> "num1, num2"
     */
    public Map<Integer, String> findPhoneNumbersByPersonIds(List<Integer> personIds) {
        if (personIds == null || personIds.isEmpty()) return Map.of();

        List<Phone> phones = phoneRepository.findByPersonIdIn(personIds);
        return phones.stream()
                .filter(p -> p.getPerson() != null && p.getPerson().getId() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getPerson().getId(),
                        Collectors.mapping(Phone::getPhoneNumber, Collectors.joining(", "))
                ));
    }

    public Phone savePhone(Phone phone) {
        return phoneRepository.save(phone);
    }

    public void deletePhone(Integer id) {
        phoneRepository.deleteById(id);
    }
}
