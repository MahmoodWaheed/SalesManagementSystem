package com.mahmoud.sales.service;

import com.mahmoud.sales.entity.Phone;
import com.mahmoud.sales.repository.PhoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PhoneService {

    @Autowired
    private PhoneRepository phoneRepository;

    public List<Phone> findAllPhones() {
        return phoneRepository.findAll();
    }

    public Optional<Phone> findPhoneById(Integer id) {
        return phoneRepository.findById(id);
    }

    public List<Phone> findPhonesByEmployeeId(Integer employeeId) {
        return phoneRepository.findByEmployeeId(employeeId);
    }

    public List<Phone> findPhonesByPersonId(Integer personId) {
        return phoneRepository.findByPersonId(personId);
    }

    public Phone savePhone(Phone phone) {
        return phoneRepository.save(phone);
    }

    public void deletePhone(Integer id) {
        phoneRepository.deleteById(id);
    }
}
