package com.mahmoud.sales.service;

import com.mahmoud.sales.entity.Transactiondetail;
import com.mahmoud.sales.repository.TransactiondetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactiondetailService {

    @Autowired
    private TransactiondetailRepository transactiondetailRepository;

    public List<Transactiondetail> findAllTransactionDetails() {
        return transactiondetailRepository.findAll();
    }

    public Optional<Transactiondetail> findTransactionDetailById(Integer id) {
        return transactiondetailRepository.findById(id);
    }

    public Transactiondetail saveTransactionDetail(Transactiondetail transactionDetail) {
        return transactiondetailRepository.save(transactionDetail);
    }

    public void deleteTransactionDetail(Integer id) {
        transactiondetailRepository.deleteById(id);
    }
    // New method to find transaction details by transaction ID
    public List<Transactiondetail> findTransactionDetailsByTransactionId(Integer transactionId) {
        return transactiondetailRepository.findByTransaction_Id(transactionId);
    }
}