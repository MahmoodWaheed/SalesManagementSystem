package com.mahmoud.sales.service;

import com.mahmoud.sales.entity.*;
import com.mahmoud.sales.repository.PaymentRepository;
import com.mahmoud.sales.repository.TransactionRepository;
import com.mahmoud.sales.repository.TransactiondetailRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactiondetailRepository transactiondetailRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TransactiondetailService transactiondetailService; // for helper like nextDetailIdForTransaction


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

    @Transactional
    public Transaction saveTransactionWithDetailsAndPayments(Transaction transaction,
                                                             List<Transactiondetail> details,
                                                             List<Payment> payments) {
        // 1. save transaction to get id
        Transaction savedTx = transactionRepository.save(transaction);

        Integer txId = savedTx.getId();

        // 2. persist details
        for (Transactiondetail detail : details) {
            if (!isDetailValid(detail)) continue; // skip empty rows
            // assign the composite id: transactionId = txId; id = next id per transaction
            Integer nextDetailId = transactiondetailService.nextDetailIdForTransaction(txId);
            TransactiondetailId tid = new TransactiondetailId(nextDetailId, txId);
            detail.setId(tid);
            detail.setTransaction(savedTx);
            transactiondetailRepository.save(detail);
        }

        // 3. persist payments
        for (Payment p : payments) {
            if (p.getAmount() == null || p.getAmount().compareTo(BigDecimal.ZERO) <= 0) continue;
            p.setTransaction(savedTx);
            // if person not set, set from transaction
            if (p.getPerson() == null && savedTx.getPerson() != null) {
                p.setPerson(savedTx.getPerson());
            }
            paymentRepository.save(p);
        }

//        // 4. compute totals again (optional)
//        BigDecimal computedTotal = details.stream()
//                .filter(this::isDetailValid)
//                .map(d -> d.getComulativePrice() != null ? d.getComulativePrice() : BigDecimal.ZERO)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        savedTx.setTotalAmount(computedTotal);
//        return transactionRepository.save(savedTx);
        // 4. Validate and recalculate total (preserving withdrawal fee)
        BigDecimal itemsTotal = details.stream()
                .filter(this::isDetailValid)
                .map(d -> d.getComulativePrice() != null ? d.getComulativePrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get the withdrawal fee (already set by controller)
        BigDecimal withdrawalFee = savedTx.getWithdrawalFee();
        if (withdrawalFee == null) {
            withdrawalFee = BigDecimal.ZERO;
        }

        // Recalculate grand total = items + fee
        BigDecimal grandTotal = itemsTotal.add(withdrawalFee);

        // Update the transaction with validated total
        savedTx.setTotalAmount(grandTotal);

        // Save and return
        return transactionRepository.save(savedTx);
    }

    private boolean isDetailValid(Transactiondetail d) {
        return d.getItem() != null && d.getQuantity()!=null && d.getQuantity() > 0
                && d.getSellingPrice() != null && d.getSellingPrice().compareTo(BigDecimal.ZERO) > 0;
    }

}
