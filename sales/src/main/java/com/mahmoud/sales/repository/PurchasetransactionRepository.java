package com.mahmoud.sales.repository;

import com.mahmoud.sales.entity.Purchasetransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchasetransactionRepository extends JpaRepository<Purchasetransaction, Integer> {
    // Additional query methods can be defined here, e.g., find by person or date range

}
