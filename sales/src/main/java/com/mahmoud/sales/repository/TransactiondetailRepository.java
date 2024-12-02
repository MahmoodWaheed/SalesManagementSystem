package com.mahmoud.sales.repository;

import com.mahmoud.sales.entity.Transactiondetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactiondetailRepository extends JpaRepository<Transactiondetail, Integer> {
    // Additional query methods can be added if needed
}