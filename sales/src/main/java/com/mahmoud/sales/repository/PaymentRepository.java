package com.mahmoud.sales.repository;

import com.mahmoud.sales.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    // Additional custom query methods can be defined here if needed
}
