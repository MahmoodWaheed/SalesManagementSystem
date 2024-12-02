package com.mahmoud.sales.service;

import com.mahmoud.sales.entity.Purchasetransaction;
import com.mahmoud.sales.repository.PurchasetransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PurchasetransactionService {

    @Autowired
    private PurchasetransactionRepository purchasetransactionRepository;

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
}
