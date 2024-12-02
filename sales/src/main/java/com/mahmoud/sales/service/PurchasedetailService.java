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
}
