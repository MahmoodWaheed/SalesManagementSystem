package com.mahmoud.sales.service;

import com.mahmoud.sales.entity.Payment;
import com.mahmoud.sales.entity.Transaction;
import com.mahmoud.sales.entity.Transactiondetail;
import com.mahmoud.sales.repository.PaymentRepository;
import com.mahmoud.sales.repository.TransactionRepository;
import com.mahmoud.sales.repository.TransactiondetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Prepare invoice data for a given transaction.
 */
@Service
public class InvoiceService {

    private final TransactionRepository transactionRepository;
    private final TransactiondetailRepository detailRepository;
    private final PaymentRepository paymentRepository;

    public InvoiceService(TransactionRepository transactionRepository,
                          TransactiondetailRepository detailRepository,
                          PaymentRepository paymentRepository) {
        this.transactionRepository = transactionRepository;
        this.detailRepository = detailRepository;
        this.paymentRepository = paymentRepository;
    }

    public static class InvoiceData {
        private final Transaction transaction;
        private final List<Transactiondetail> details;
        private final List<Payment> payments;
        private final BigDecimal subTotal;
        private final BigDecimal taxPercent;
        private final BigDecimal taxAmount;
        private final BigDecimal grandTotal;

        public InvoiceData(Transaction transaction,
                           List<Transactiondetail> details,
                           List<Payment> payments,
                           BigDecimal subTotal,
                           BigDecimal taxPercent,
                           BigDecimal taxAmount,
                           BigDecimal grandTotal) {
            this.transaction = transaction;
            this.details = details;
            this.payments = payments;
            this.subTotal = subTotal;
            this.taxPercent = taxPercent;
            this.taxAmount = taxAmount;
            this.grandTotal = grandTotal;
        }

        public Transaction getTransaction() { return transaction; }
        public List<Transactiondetail> getDetails() { return details; }
        public List<Payment> getPayments() { return payments; }
        public BigDecimal getSubTotal() { return subTotal; }
        public BigDecimal getTaxPercent() { return taxPercent; }
        public BigDecimal getTaxAmount() { return taxAmount; }
        public BigDecimal getGrandTotal() { return grandTotal; }
    }

    /**
     * Load full invoice data for a transaction id.
     * Uses transactional read-only context to allow lazy init.
     * EAGERLY loads all relationships to avoid LazyInitializationException.
     */
    @Transactional(readOnly = true)
    public InvoiceData prepareInvoice(Integer transactionId) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        // Eagerly initialize lazy relationships
        if (tx.getPerson() != null) {
            // Force initialization of person
            tx.getPerson().getName();
            tx.getPerson().getLocation();
            tx.getPerson().getOpenBalance();
        }

        if (tx.getSalesRep() != null) {
            // Force initialization of employee
            tx.getSalesRep().getName();
            tx.getSalesRep().getRole();
        }

        // Load transaction details with items
        List<Transactiondetail> details = detailRepository.findByTransaction_Id(tx.getId());

        // Force initialization of items in details
        for (Transactiondetail detail : details) {
            if (detail.getItem() != null) {
                detail.getItem().getName();
                detail.getItem().getId();
            }
        }

        // Load payments
        List<Payment> payments = paymentRepository.findByTransactionId(tx.getId());

        // Force initialization of payment relationships
        for (Payment payment : payments) {
            if (payment.getPerson() != null) {
                payment.getPerson().getName();
            }
        }

        // Calculate totals
        BigDecimal subTotal = details.stream()
                .map(d -> d.getComulativePrice() == null ? BigDecimal.ZERO : d.getComulativePrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxPercent = BigDecimal.ZERO; // change to desired percent if needed
        BigDecimal taxAmount = subTotal.multiply(taxPercent).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal grandTotal = subTotal.add(taxAmount);

        return new InvoiceData(tx, details, payments, subTotal, taxPercent, taxAmount, grandTotal);
    }
}