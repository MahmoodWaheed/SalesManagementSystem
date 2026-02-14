package com.mahmoud.sales.service;

import com.mahmoud.sales.entity.Person;
import com.mahmoud.sales.repository.PersonRepository;
import com.mahmoud.sales.repository.PhoneRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

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
    /**
     * ✅ New: Save and return the persisted entity (needed to create Phone relation safely).
     */
    public Person savePersonAndReturn(Person person) {
        return personRepository.save(person);
    }

    public void deletePerson(Integer id) {
        personRepository.deleteById(id);
    }

    public Person findPersonByName(String name) {
        return personRepository.findByName(name);
    }

    public List<Object[]> getPersonRemainingBalance() {
        return personRepository.getPersonRemainingBalance();
    }

    public BigDecimal calculateRemainingBalance(Integer personId) {
        BigDecimal transactionAmount = personRepository.findTotalTransactionAmountByPersonId(personId);
        BigDecimal paymentAmount = personRepository.findTotalPaymentAmountByPersonId(personId);

        if (transactionAmount == null) transactionAmount = BigDecimal.ZERO;
        if (paymentAmount == null) paymentAmount = BigDecimal.ZERO;

        return transactionAmount.subtract(paymentAmount);
    }

    public List<Person> findByType(String type) {
        return personRepository.findByType(type);
    }

    /**
     * Server-side paging + optional search + server-side sorting.
     * sortField: "name" or "openBalance"
     */
    public Page<Person> findByTypePaged(String type, String search, int pageIndex, int pageSize, String sortField, boolean asc) {
        int safeIndex = Math.max(pageIndex, 0);
        int safeSize = Math.max(pageSize, 1);

        String field = (sortField == null || sortField.isBlank()) ? "name" : sortField.trim();

        // Safe allow-list (avoid invalid properties)
        if (!field.equals("name") && !field.equals("openBalance")) {
            field = "name";
        }

        Sort sort = Sort.by(asc ? Sort.Direction.ASC : Sort.Direction.DESC, field);
        Pageable pageable = PageRequest.of(safeIndex, safeSize, sort);

        String q = (search == null) ? "" : search.trim();
        if (q.isBlank()) {
            return personRepository.findByType(type, pageable);
        }
        return personRepository.searchByType(type, q, pageable);
    }

    public Map<Integer, BigDecimal> getRemainingBalanceByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();

        List<Object[]> rows = personRepository.getRemainingBalanceByIds(ids);
        Map<Integer, BigDecimal> map = new HashMap<>();
        for (Object[] r : rows) {
            if (r == null || r.length < 2) continue;
            Integer id = ((Number) r[0]).intValue();
            BigDecimal remaining = (r[1] == null) ? BigDecimal.ZERO : (BigDecimal) r[1];
            map.put(id, remaining);
        }
        return map;
    }

    public Totals getTotalsByTypeAndSearch(String type, String search) {
        String q = (search == null) ? "" : search.trim();
        Object[] row = personRepository.getTotalsByTypeAndSearch(type, q);

        if (row == null || row.length < 3) {
            return new Totals(0L, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        long count = (row[0] == null) ? 0L : ((Number) row[0]).longValue();
        BigDecimal sumOpen = (row[1] == null) ? BigDecimal.ZERO : (BigDecimal) row[1];
        BigDecimal sumRemaining = (row[2] == null) ? BigDecimal.ZERO : (BigDecimal) row[2];

        return new Totals(count, sumOpen, sumRemaining);
    }

    public record Totals(long count, BigDecimal sumOpenBalance, BigDecimal sumRemainingBalance) {}
}
