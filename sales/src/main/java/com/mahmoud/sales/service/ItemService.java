package com.mahmoud.sales.service;

import com.mahmoud.sales.entity.Item;
import com.mahmoud.sales.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    /**
     * ✅ Return active only (for any legacy code calling findAllItems)
     */
    public List<Item> findAllItems() {
        // If you still have code calling this, it will return only active
        return itemRepository.findAll().stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsActive()))
                .toList();
    }

    /**
     * ✅ Only active items should be fetched in UI.
     */
    public Optional<Item> findItemById(Integer id) {
        return itemRepository.findActiveById(id);
    }

    /**
     * ✅ Ensure saved items are active by default.
     */
    public Item saveItem(Item item) {
        if (item.getIsActive() == null) item.setIsActive(true);
        return itemRepository.save(item);
    }

    /**
     * ❌ Old hard delete (keep it, but DON’T use it from UI)
     */
    public void deleteItem(Integer id) {
        itemRepository.deleteById(id);
    }

    /**
     * ✅ Soft delete used by UI
     */
    @Transactional
    public void softDeleteItem(Integer id) {
        int updated = itemRepository.softDeleteById(id);
        if (updated == 0) {
            throw new IllegalStateException("Item not found or already archived.");
        }
    }

    /**
     * Server-side paging + sorting + search (ACTIVE ONLY)
     * sortField allow-list: name, itemBalance, sellingPrice, purchasingPrice
     */
    public Page<Item> findItemsPaged(String search, int pageIndex, int pageSize, String sortField, boolean asc) {
        int safeIndex = Math.max(pageIndex, 0);
        int safeSize = Math.max(pageSize, 1);

        String field = (sortField == null || sortField.isBlank()) ? "name" : sortField.trim();
        if (!field.equals("name")
                && !field.equals("itemBalance")
                && !field.equals("sellingPrice")
                && !field.equals("purchasingPrice")) {
            field = "name";
        }

        Sort sort = Sort.by(asc ? Sort.Direction.ASC : Sort.Direction.DESC, field);
        Pageable pageable = PageRequest.of(safeIndex, safeSize, sort);

        String q = (search == null) ? "" : search.trim();
        if (q.isBlank()) {
            return itemRepository.findAllActive(pageable);
        }
        return itemRepository.searchActive(q, pageable);
    }
}
