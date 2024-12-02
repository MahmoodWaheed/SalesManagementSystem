package com.mahmoud.sales.repository;

import com.mahmoud.sales.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    // Additional query methods can be defined here, e.g., find by person or transaction type
}
