package com.mahmoud.sales.service;

import com.mahmoud.sales.entity.*;
import com.mahmoud.sales.repository.PaymentRepository;
import com.mahmoud.sales.repository.PurchasedetailRepository;
import com.mahmoud.sales.repository.PurchasetransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PurchasetransactionService {

    @Autowired
    private PurchasetransactionRepository purchasetransactionRepository;

    @Autowired
    private PurchasedetailRepository purchasedetailRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PurchasedetailService purchasedetailService; // for helper like nextDetailIdForPurchase

    public List<Purchasetransaction> findAllPurchasetransactions() {
        return purchasetransactionRepository.findAll();
    }

    public Optional<Purchasetransaction> findPurchasetransactionById(Integer id) {
        return purchasetransactionRepository.findById(id);
    }

    public Purchasetransaction savePurchasetransaction(Purchasetransaction purchasetransaction) {
        return purchasetransactionRepository.save(purchasetransaction);
    }

    public void deletePurchasetransaction(Integer id) {
        purchasetransactionRepository.deleteById(id);
    }

    /**
     * Save purchase transaction with details and payments in a single transaction
     * FIXED: Properly handle composite primary key sequencing
     */
    @Transactional
    public Purchasetransaction savePurchaseWithDetailsAndPayments(Purchasetransaction purchasetransaction,
                                                                  List<Purchasedetail> details,
                                                                  List<Payment> payments) {
        // 1. Save purchase transaction to get ID
        Purchasetransaction saved = purchasetransactionRepository.save(purchasetransaction);
        Integer purchaseId = saved.getId();

        // 2. Persist details with proper sequential IDs
        // CRITICAL FIX: Query the database to get the next available ID for this purchase
        // This handles both new purchases (starts at 1) and updates (continues from max ID)
        for (Purchasedetail detail : details) {
            if (!isDetailValid(detail)) continue; // skip empty rows

            // Get next available ID for this specific purchase transaction
            Integer nextDetailId = purchasedetailService.nextDetailIdForPurchase(purchaseId);

            // Create composite ID
            PurchasedetailId detailId = new PurchasedetailId();
            detailId.setId(nextDetailId);
            detailId.setPurchasetransactionId(purchaseId);

            detail.setId(detailId);
            detail.setPurchaseTransaction(saved);
            purchasedetailRepository.save(detail);
        }

        // 3. Persist payments
        for (Payment p : payments) {
            if (p.getAmount() == null || p.getAmount().compareTo(BigDecimal.ZERO) <= 0) continue;

            p.setPurchaseTransaction(saved);

            // If person not set, set from purchase transaction
            if (p.getPerson() == null && saved.getPerson() != null) {
                p.setPerson(saved.getPerson());
            }

            paymentRepository.save(p);
        }

        // 4. Validate and recalculate total (preserving withdrawal fee)
        BigDecimal itemsTotal = details.stream()
                .filter(this::isDetailValid)
                .map(d -> d.getComulativePrice() != null ? d.getComulativePrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get the withdrawal fee (already set by controller)
        BigDecimal withdrawalFee = saved.getWithdrawalFee();
        if (withdrawalFee == null) {
            withdrawalFee = BigDecimal.ZERO;
        }

        // Recalculate grand total = items + fee
        BigDecimal grandTotal = itemsTotal.add(withdrawalFee);

        // Update the purchase transaction with validated total
        saved.setTotalAmount(grandTotal);

        // Save and return
        return purchasetransactionRepository.save(saved);
    }

    private boolean isDetailValid(Purchasedetail d) {
        return d.getItem() != null
                && d.getQuantity() != null && d.getQuantity() > 0
                && d.getPurchasingPrice() != null && d.getPurchasingPrice().compareTo(BigDecimal.ZERO) > 0;
    }
}