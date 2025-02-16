package com.mahmoud.sales.repository;

import com.mahmoud.sales.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Integer> {
    Person findByName(String name);
    // Custom query methods can be defined here if needed
    @Query(value = """
    WITH TotalTransactions AS (
        SELECT t.Person_id, SUM(t.total_amount) AS total_transaction_amount
        FROM `transaction` t
        GROUP BY t.Person_id
    ),
    TotalPayments AS (
        SELECT py.Person_id, SUM(py.amount) AS total_payment_amount
        FROM payment py
        GROUP BY py.Person_id
    )
    SELECT p.id AS person_id, p.name AS person_name, 
           IFNULL(tt.total_transaction_amount, 0) AS total_transaction_amount, 
           IFNULL(tp.total_payment_amount, 0) AS total_payment_amount, 
           (IFNULL(tt.total_transaction_amount, 0) - IFNULL(tp.total_payment_amount, 0)) AS remaining_balance
    FROM person p
    LEFT JOIN TotalTransactions tt ON p.id = tt.Person_id
    LEFT JOIN TotalPayments tp ON p.id = tp.Person_id
    """, nativeQuery = true)
    List<Object[]> getPersonRemainingBalance();

    // Query to get the total transaction amount for a person from the 'transaction' table
//    @Query(value = "SELECT SUM(t.total_amount) FROM transaction t WHERE t.person_id = :personId", nativeQuery = true)
//    BigDecimal findTotalTransactionAmountByPersonId(@Param("personId") Integer personId);



    // Query to get the total transaction amount for a person from the 'transaction' table
    @Query("SELECT SUM(t.totalAmount) FROM Transaction t WHERE t.person.id = :personId")
    BigDecimal findTotalTransactionAmountByPersonId(@Param("personId") Integer personId);


    // Query to get the total payment amount for a person from the 'payment' table
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.person.id = :personId")
    BigDecimal findTotalPaymentAmountByPersonId(@Param("personId") Integer personId);

    public List<Person> findByType(String type);
}
