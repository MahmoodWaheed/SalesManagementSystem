package com.mahmoud.sales.service;

import com.mahmoud.sales.entity.Transactiondetail;
import com.mahmoud.sales.repository.TransactiondetailRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactiondetailService {

    @Autowired
    private TransactiondetailRepository transactiondetailRepository;

    @Autowired
    private TransactiondetailRepository repository;

    // Return next detail id (integer) for given transaction id.
    @Transactional(readOnly = true)
    public Integer nextDetailIdForTransaction(Integer transactionId) {
        Integer maxId = repository.findMaxIdByTransactionId(transactionId);
        return (maxId == null) ? 1 : maxId + 1;
    }

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