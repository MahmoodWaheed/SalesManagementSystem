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
import java.util.Optional;

/**
 * Service preparing invoice DTO-like data for the UI.
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
     * This ensures related collections are loaded within a transactional context.
     */
    @Transactional(readOnly = true)
    public InvoiceData prepareInvoice(Integer transactionId) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        // Load details & payments
        List<Transactiondetail> details = detailRepository.findByTransactionId(tx.getId());
        List<Payment> payments = paymentRepository.findByTransactionId(tx.getId());

        // compute subtotal and tax (example: tax 14% if you want)
        BigDecimal subTotal = details.stream()
                .map(d -> d.getComulativePrice() == null ? BigDecimal.ZERO : d.getComulativePrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxPercent = BigDecimal.valueOf(0); // default 0%
        // If you want 14% tax set taxPercent = BigDecimal.valueOf(14);
        BigDecimal taxAmount = subTotal.multiply(taxPercent).divide(BigDecimal.valueOf(100));
        BigDecimal grandTotal = subTotal.add(taxAmount);

        return new InvoiceData(tx, details, payments, subTotal, taxPercent, taxAmount, grandTotal);
    }
}
