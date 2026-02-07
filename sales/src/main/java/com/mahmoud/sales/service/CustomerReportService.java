package com.mahmoud.sales.service;

import com.mahmoud.sales.entity.*;
import com.mahmoud.sales.repository.PaymentRepository;
import com.mahmoud.sales.repository.PersonRepository;
import com.mahmoud.sales.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerReportService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    /**
     * Generate comprehensive customer transaction history report
     */
    @Transactional(readOnly = true)
    public CustomerReportData generateCustomerReport(Integer customerId,
                                                     Instant startDate,
                                                     Instant endDate) {
        // Load customer
        Person customer = personRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        // Get all transactions for this customer in date range
        List<Transaction> allTransactions = transactionRepository.findAll();
        List<Transaction> customerTransactions = allTransactions.stream()
                .filter(t -> t.getPerson() != null && t.getPerson().getId().equals(customerId))
                .filter(t -> isInDateRange(t.getTransactionDate(), startDate, endDate))
                .sorted((t1, t2) -> t1.getTransactionDate().compareTo(t2.getTransactionDate()))
                .collect(Collectors.toList());

        // Force lazy loading
        for (Transaction t : customerTransactions) {
            if (t.getSalesRep() != null) {
                t.getSalesRep().getName();
            }
        }

        // Get all payments for this customer in date range
        List<Payment> allPayments = paymentRepository.findAll();
        List<Payment> customerPayments = allPayments.stream()
                .filter(p -> p.getPerson() != null && p.getPerson().getId().equals(customerId))
                .filter(p -> isInDateRange(p.getPaymentDate(), startDate, endDate))
                .sorted((p1, p2) -> p1.getPaymentDate().compareTo(p2.getPaymentDate()))
                .collect(Collectors.toList());

        // Calculate totals
        BigDecimal totalTransactionAmount = customerTransactions.stream()
                .map(t -> t.getTotalAmount() != null ? t.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPayments = customerPayments.stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal openingBalance = customer.getOpenBalance() != null ?
                customer.getOpenBalance() : BigDecimal.ZERO;

        // Calculate current balance
        // Formula: Opening Balance + Total Transactions - Total Payments
        BigDecimal currentBalance = openingBalance
                .add(totalTransactionAmount)
                .subtract(totalPayments);

        return new CustomerReportData(
                customer,
                customerTransactions,
                customerPayments,
                openingBalance,
                totalTransactionAmount,
                totalPayments,
                currentBalance,
                startDate,
                endDate
        );
    }

    /**
     * Get customer summary (for dashboard/overview)
     */
    @Transactional(readOnly = true)
    public CustomerSummary getCustomerSummary(Integer customerId) {
        Person customer = personRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        // Get all transactions
        List<Transaction> allTransactions = transactionRepository.findAll();
        List<Transaction> customerTransactions = allTransactions.stream()
                .filter(t -> t.getPerson() != null && t.getPerson().getId().equals(customerId))
                .collect(Collectors.toList());

        // Get all payments
        List<Payment> allPayments = paymentRepository.findAll();
        List<Payment> customerPayments = allPayments.stream()
                .filter(p -> p.getPerson() != null && p.getPerson().getId().equals(customerId))
                .collect(Collectors.toList());

        BigDecimal totalTransactions = customerTransactions.stream()
                .map(t -> t.getTotalAmount() != null ? t.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPayments = customerPayments.stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal openingBalance = customer.getOpenBalance() != null ?
                customer.getOpenBalance() : BigDecimal.ZERO;

        BigDecimal currentBalance = openingBalance.add(totalTransactions).subtract(totalPayments);

        return new CustomerSummary(
                customer,
                customerTransactions.size(),
                customerPayments.size(),
                openingBalance,
                totalTransactions,
                totalPayments,
                currentBalance
        );
    }

    private boolean isInDateRange(Instant date, Instant start, Instant end) {
        if (date == null) return false;
        if (start != null && date.isBefore(start)) return false;
        if (end != null && date.isAfter(end)) return false;
        return true;
    }

    /**
     * Data class for comprehensive customer report
     */
    public static class CustomerReportData {
        private final Person customer;
        private final List<Transaction> transactions;
        private final List<Payment> payments;
        private final BigDecimal openingBalance;
        private final BigDecimal totalTransactions;
        private final BigDecimal totalPayments;
        private final BigDecimal currentBalance;
        private final Instant startDate;
        private final Instant endDate;

        public CustomerReportData(Person customer,
                                  List<Transaction> transactions,
                                  List<Payment> payments,
                                  BigDecimal openingBalance,
                                  BigDecimal totalTransactions,
                                  BigDecimal totalPayments,
                                  BigDecimal currentBalance,
                                  Instant startDate,
                                  Instant endDate) {
            this.customer = customer;
            this.transactions = transactions;
            this.payments = payments;
            this.openingBalance = openingBalance;
            this.totalTransactions = totalTransactions;
            this.totalPayments = totalPayments;
            this.currentBalance = currentBalance;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public Person getCustomer() { return customer; }
        public List<Transaction> getTransactions() { return transactions; }
        public List<Payment> getPayments() { return payments; }
        public BigDecimal getOpeningBalance() { return openingBalance; }
        public BigDecimal getTotalTransactions() { return totalTransactions; }
        public BigDecimal getTotalPayments() { return totalPayments; }
        public BigDecimal getCurrentBalance() { return currentBalance; }
        public Instant getStartDate() { return startDate; }
        public Instant getEndDate() { return endDate; }
    }

    /**
     * Data class for customer summary
     */
    public static class CustomerSummary {
        private final Person customer;
        private final int transactionCount;
        private final int paymentCount;
        private final BigDecimal openingBalance;
        private final BigDecimal totalTransactions;
        private final BigDecimal totalPayments;
        private final BigDecimal currentBalance;

        public CustomerSummary(Person customer,
                               int transactionCount,
                               int paymentCount,
                               BigDecimal openingBalance,
                               BigDecimal totalTransactions,
                               BigDecimal totalPayments,
                               BigDecimal currentBalance) {
            this.customer = customer;
            this.transactionCount = transactionCount;
            this.paymentCount = paymentCount;
            this.openingBalance = openingBalance;
            this.totalTransactions = totalTransactions;
            this.totalPayments = totalPayments;
            this.currentBalance = currentBalance;
        }

        public Person getCustomer() { return customer; }
        public int getTransactionCount() { return transactionCount; }
        public int getPaymentCount() { return paymentCount; }
        public BigDecimal getOpeningBalance() { return openingBalance; }
        public BigDecimal getTotalTransactions() { return totalTransactions; }
        public BigDecimal getTotalPayments() { return totalPayments; }
        public BigDecimal getCurrentBalance() { return currentBalance; }
    }
}