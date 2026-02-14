package com.mahmoud.sales.repository;

import com.mahmoud.sales.entity.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Integer> {

    Person findByName(String name);

    List<Person> findByType(String type);

    // Server-side paging + sorting (via Pageable)
    Page<Person> findByType(String type, Pageable pageable);

    // Server-side search + paging + sorting (via Pageable)
    @Query("""
            SELECT p FROM Person p
            WHERE p.type = :type
              AND (
                    :q IS NULL OR :q = ''
                    OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(p.location) LIKE LOWER(CONCAT('%', :q, '%'))
                  )
            """)
    Page<Person> searchByType(@Param("type") String type, @Param("q") String q, Pageable pageable);

    // Existing (kept for backward compatibility)
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

    @Query("SELECT SUM(t.totalAmount) FROM Transaction t WHERE t.person.id = :personId")
    BigDecimal findTotalTransactionAmountByPersonId(@Param("personId") Integer personId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.person.id = :personId")
    BigDecimal findTotalPaymentAmountByPersonId(@Param("personId") Integer personId);

    /**
     * Batch remaining balance for a list of person IDs.
     * remaining = SUM(transactions) - SUM(payments)
     * returns rows: [person_id, remaining_balance]
     */
    @Query(value = """
            SELECT p.id AS person_id,
                   (IFNULL(tt.total_transaction_amount, 0) - IFNULL(tp.total_payment_amount, 0)) AS remaining_balance
            FROM person p
            LEFT JOIN (
                SELECT t.Person_id, SUM(t.total_amount) AS total_transaction_amount
                FROM `transaction` t
                WHERE t.Person_id IN (:ids)
                GROUP BY t.Person_id
            ) tt ON p.id = tt.Person_id
            LEFT JOIN (
                SELECT py.Person_id, SUM(py.amount) AS total_payment_amount
                FROM payment py
                WHERE py.Person_id IN (:ids)
                GROUP BY py.Person_id
            ) tp ON p.id = tp.Person_id
            WHERE p.id IN (:ids)
            """, nativeQuery = true)
    List<Object[]> getRemainingBalanceByIds(@Param("ids") List<Integer> ids);

    /**
     * Totals for type + optional search:
     * returns single row: [count, sum_open_balance, sum_remaining_balance(trans-pay)]
     */
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
            SELECT
                COUNT(p.id) AS cnt,
                IFNULL(SUM(p.open_balance), 0) AS sum_open_balance,
                IFNULL(SUM(IFNULL(tt.total_transaction_amount, 0) - IFNULL(tp.total_payment_amount, 0)), 0) AS sum_remaining_balance
            FROM person p
            LEFT JOIN TotalTransactions tt ON p.id = tt.Person_id
            LEFT JOIN TotalPayments tp ON p.id = tp.Person_id
            WHERE p.type = :type
              AND (
                    :q IS NULL OR :q = ''
                    OR p.name LIKE CONCAT('%', :q, '%')
                    OR p.location LIKE CONCAT('%', :q, '%')
                  )
            """, nativeQuery = true)
    Object[] getTotalsByTypeAndSearch(@Param("type") String type, @Param("q") String q);
}
