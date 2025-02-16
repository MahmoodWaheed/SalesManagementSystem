package com.mahmoud.sales.repository;

import com.mahmoud.sales.entity.Transactiondetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactiondetailRepository extends JpaRepository<Transactiondetail, Integer> {
    // Additional query methods can be added if needed

    // Spring Data JPA will automatically implement this method based on naming convention.
    List<Transactiondetail> findByTransaction_Id(Integer transactionId);
}