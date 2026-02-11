package com.mahmoud.sales.service;

import com.mahmoud.sales.entity.Purchasedetail;
import com.mahmoud.sales.entity.PurchasedetailId;
import com.mahmoud.sales.repository.PurchasedetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PurchasedetailService {

    @Autowired
    private PurchasedetailRepository purchasedetailRepository;

    @Autowired
    private PurchasedetailRepository repository;

    /**
     * Return next detail id (integer) for given purchase transaction id.
     * CRITICAL: No @Transactional annotation here!
     * This method must run in the SAME transaction as the caller to see uncommitted data.
     */
    public Integer nextDetailIdForPurchase(Integer purchaseTransactionId) {
        Integer maxId = repository.findMaxIdByPurchaseTransactionId(purchaseTransactionId);
        return (maxId == null) ? 1 : maxId + 1;
    }

    public List<Purchasedetail> findAllPurchasedetails() {
        return purchasedetailRepository.findAll();
    }

    public Optional<Purchasedetail> findPurchasedetailById(PurchasedetailId id) {
        return purchasedetailRepository.findById(id);
    }

    public Purchasedetail savePurchasedetail(Purchasedetail purchasedetail) {
        return purchasedetailRepository.save(purchasedetail);
    }

    public void deletePurchasedetail(PurchasedetailId id) {
        purchasedetailRepository.deleteById(id);
    }

    /**
     * Find purchase details by purchase transaction ID
     */
    public List<Purchasedetail> findPurchaseDetailsByPurchaseTransactionId(Integer purchaseTransactionId) {
        return purchasedetailRepository.findByPurchaseTransaction_Id(purchaseTransactionId);
    }
}