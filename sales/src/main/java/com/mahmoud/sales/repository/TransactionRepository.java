package com.mahmoud.sales.repository;

import com.mahmoud.sales.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    // Additional query methods can be defined here, e.g., find by person or transaction type
    // TransactionRepository.java (JPA)
    @Query(value = "SELECT nextval('id')", nativeQuery = true)
    Integer getNextTransactionIdSequence();

    // Finds the transaction with an ID less than the given ID,
    // ordered in descending order (so the first result is the previous one)
    Optional<Transaction> findTopByIdLessThanOrderByIdDesc(Integer currentTransactionId);

}
