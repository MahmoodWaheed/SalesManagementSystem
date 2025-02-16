package com.mahmoud.sales.service;

import com.mahmoud.sales.entity.Employee;
import com.mahmoud.sales.entity.Person;
import com.mahmoud.sales.entity.Transaction;
import com.mahmoud.sales.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public List<Transaction> findAllTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> findTransactionById(Integer id) {
        return transactionRepository.findById(id);
    }

    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(Integer id) {
        transactionRepository.deleteById(id);
    }
    // TransactionService.java
    public Integer getNextTransactionId() {
        // Create a temporary transaction to get the next ID
        Transaction tempTransaction = new Transaction();
        tempTransaction = transactionRepository.save(tempTransaction);
        transactionRepository.delete(tempTransaction); // Clean up
        return tempTransaction.getId();
    }
    public Transaction createTransaction(Employee salesRep, Person customer) {
        Transaction transaction = new Transaction();
        transaction.setSalesRep(salesRep);
        transaction.setPerson(customer);
        transaction.setTransactionDate(Instant.now());
        return transactionRepository.save(transaction);
    }

    // New method to get the previous transaction based on the current transaction's ID
    public Optional<Transaction> findPreviousTransaction(Integer currentTransactionId) {
        return transactionRepository.findTopByIdLessThanOrderByIdDesc(currentTransactionId);
    }
}
