package com.mahmoud.sales.repository;

import com.mahmoud.sales.entity.Item;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    /**
     * ✅ Only active items
     */
    @Query("SELECT i FROM Item i WHERE i.isActive = true")
    Page<Item> findAllActive(Pageable pageable);

    /**
     * ✅ Active-only search + paging + sorting (sorting from Pageable)
     * Search: name + description
     */
    @Query("""
            SELECT i FROM Item i
            WHERE i.isActive = true
              AND (
                    :q IS NULL OR :q = ''
                    OR LOWER(i.name) LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(i.description) LIKE LOWER(CONCAT('%', :q, '%'))
                  )
            """)
    Page<Item> searchActive(@Param("q") String q, Pageable pageable);

    /**
     * ✅ Active-only get by id (used by view/edit if needed)
     */
    @Query("SELECT i FROM Item i WHERE i.id = :id AND i.isActive = true")
    Optional<Item> findActiveById(@Param("id") Integer id);

    /**
     * ✅ Soft delete (archive)
     */
    @Modifying
    @Transactional
    @Query("UPDATE Item i SET i.isActive = false WHERE i.id = :id")
    int softDeleteById(@Param("id") Integer id);
}
