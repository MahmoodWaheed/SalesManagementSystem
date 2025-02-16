package com.mahmoud.sales.service;

import com.mahmoud.sales.entity.Person;
import com.mahmoud.sales.entity.Phone;
import com.mahmoud.sales.repository.PersonRepository;
import com.mahmoud.sales.repository.PhoneRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Component
@AllArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    @Autowired
    private PhoneRepository phoneRepository;

    public List<Person> findAllPersons() {
        return personRepository.findAll();
    }

    public Optional<Person> findPersonById(Integer id) {
        return personRepository.findById(id);
    }

    public void savePerson(Person person) {
        personRepository.save(person);
    }

    public void deletePerson(Integer id) {
        personRepository.deleteById(id);
    }

    public Person findPersonByName(String name) {
        return personRepository.findByName(name);
    }


    // New method to get transaction and payment details
    public List<Object[]> getPersonRemainingBalance() {
        return personRepository.getPersonRemainingBalance();
    }

    // Function to calculate transaction details (transaction amount, payment amount, balance)
    public BigDecimal calculateRemainingBalance(Integer personId) {
        BigDecimal transactionAmount = personRepository.findTotalTransactionAmountByPersonId(personId);
        BigDecimal paymentAmount = personRepository.findTotalPaymentAmountByPersonId(personId);

        // Default to 0 if either value is null
        if (transactionAmount == null) {
            transactionAmount = BigDecimal.ZERO;
        }
        if (paymentAmount == null) {
            paymentAmount = BigDecimal.ZERO;
        }

        // Calculate the remaining balance
        return transactionAmount.subtract(paymentAmount);
    }
    public List<Person> findByType(String type) {
        return personRepository.findByType(type);
    }
}
