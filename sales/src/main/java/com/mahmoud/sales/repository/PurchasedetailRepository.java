package com.mahmoud.sales.repository;

import com.mahmoud.sales.entity.Purchasedetail;
import com.mahmoud.sales.entity.PurchasedetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchasedetailRepository extends JpaRepository<Purchasedetail, PurchasedetailId> {

    /**
     * Find the maximum detail ID for a specific purchase transaction.
     * Used to generate the next sequential detail ID.
     */
    @Query("SELECT MAX(pd.id.id) FROM Purchasedetail pd WHERE pd.id.purchasetransactionId = :purchaseTransactionId")
    Integer findMaxIdByPurchaseTransactionId(@Param("purchaseTransactionId") Integer purchaseTransactionId);

    /**
     * Find all purchase details for a specific purchase transaction.
     * Spring Data JPA automatically implements this based on naming convention.
     */
    List<Purchasedetail> findByPurchaseTransaction_Id(Integer purchaseTransactionId);
}