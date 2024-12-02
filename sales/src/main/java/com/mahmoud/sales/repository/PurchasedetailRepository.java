package com.mahmoud.sales.repository;

import com.mahmoud.sales.entity.Purchasedetail;
import com.mahmoud.sales.entity.PurchasedetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchasedetailRepository extends JpaRepository<Purchasedetail, PurchasedetailId> {
    // Additional query methods if needed, for example, by transaction or item
}
