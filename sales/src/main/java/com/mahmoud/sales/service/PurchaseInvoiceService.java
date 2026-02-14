package com.mahmoud.sales.service;

import com.mahmoud.sales.entity.Payment;
import com.mahmoud.sales.entity.Purchasedetail;
import com.mahmoud.sales.entity.Purchasetransaction;
import com.mahmoud.sales.repository.PaymentRepository;
import com.mahmoud.sales.repository.PurchasedetailRepository;
import com.mahmoud.sales.repository.PurchasetransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Prepare purchase invoice data for a given purchase transaction.
 */
@Service
public class PurchaseInvoiceService {

    private final PurchasetransactionRepository purchaseTransactionRepository;
    private final PurchasedetailRepository purchaseDetailRepository;
    private final PaymentRepository paymentRepository;

    public PurchaseInvoiceService(PurchasetransactionRepository purchaseTransactionRepository,
                                  PurchasedetailRepository purchaseDetailRepository,
                                  PaymentRepository paymentRepository) {
        this.purchaseTransactionRepository = purchaseTransactionRepository;
        this.purchaseDetailRepository = purchaseDetailRepository;
        this.paymentRepository = paymentRepository;
    }

    public static class PurchaseInvoiceData {
        private final Purchasetransaction purchaseTransaction;
        private final List<Purchasedetail> details;
        private final List<Payment> payments;
        private final BigDecimal subTotal;
        private final BigDecimal taxPercent;
        private final BigDecimal taxAmount;
        private final BigDecimal grandTotal;

        public PurchaseInvoiceData(Purchasetransaction purchaseTransaction,
                                   List<Purchasedetail> details,
                                   List<Payment> payments,
                                   BigDecimal subTotal,
                                   BigDecimal taxPercent,
                                   BigDecimal taxAmount,
                                   BigDecimal grandTotal) {
            this.purchaseTransaction = purchaseTransaction;
            this.details = details;
            this.payments = payments;
            this.subTotal = subTotal;
            this.taxPercent = taxPercent;
            this.taxAmount = taxAmount;
            this.grandTotal = grandTotal;
        }

        public Purchasetransaction getPurchaseTransaction() { return purchaseTransaction; }
        public List<Purchasedetail> getDetails() { return details; }
        public List<Payment> getPayments() { return payments; }
        public BigDecimal getSubTotal() { return subTotal; }
        public BigDecimal getTaxPercent() { return taxPercent; }
        public BigDecimal getTaxAmount() { return taxAmount; }
        public BigDecimal getGrandTotal() { return grandTotal; }
    }

    /**
     * Load full purchase invoice data for a purchase transaction id.
     * Uses transactional read-only context to allow lazy init.
     * EAGERLY loads all relationships to avoid LazyInitializationException.
     */
    @Transactional(readOnly = true)
    public PurchaseInvoiceData preparePurchaseInvoice(Integer purchaseTransactionId) {
        Purchasetransaction tx = purchaseTransactionRepository.findById(purchaseTransactionId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase transaction not found: " + purchaseTransactionId));

        // Eagerly initialize lazy relationships
        if (tx.getPerson() != null) {
            // Force initialization of supplier
            tx.getPerson().getName();
            tx.getPerson().getLocation();
            tx.getPerson().getOpenBalance();
        }

        // Force initialization of sales rep (employee)
        if (tx.getSalesRep() != null) {
            // Access multiple properties to ensure full initialization
            tx.getSalesRep().getId();
            tx.getSalesRep().getName();
            // Force Hibernate to initialize the proxy
            org.hibernate.Hibernate.initialize(tx.getSalesRep());
        }

        // Load purchase details with items
        List<Purchasedetail> details = purchaseDetailRepository.findAll().stream()
                .filter(d -> d.getPurchaseTransaction() != null &&
                        d.getPurchaseTransaction().getId().equals(tx.getId()))
                .collect(Collectors.toList());

        // Force initialization of items in details
        for (Purchasedetail detail : details) {
            if (detail.getItem() != null) {
                detail.getItem().getName();
                detail.getItem().getId();
            }
        }

        // Load payments for this purchase transaction
        List<Payment> payments = paymentRepository.findAll().stream()
                .filter(p -> p.getPurchaseTransaction() != null &&
                        p.getPurchaseTransaction().getId().equals(tx.getId()))
                .collect(Collectors.toList());

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

        return new PurchaseInvoiceData(tx, details, payments, subTotal, taxPercent, taxAmount, grandTotal);
    }
}