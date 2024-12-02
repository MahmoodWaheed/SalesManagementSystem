package com.mahmoud.sales.repository;

import com.mahmoud.sales.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
    // Additional custom query methods can be defined here if needed
}
