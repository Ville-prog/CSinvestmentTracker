/**
 * InventoryService.java
 *
 * Service for building the tracked inventory view from database records.
 * Joins Item entities with their latest Price record to produce per-item value and P&L data.
 * Does not call any external APIs — all data is read from the local database.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.service;

import com.cstracker.entity.Item;
import com.cstracker.entity.Price;
import com.cstracker.model.InventoryItemView;
import com.cstracker.repository.ItemRepository;
import com.cstracker.repository.PriceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private final ItemRepository itemRepository;
    private final PriceRepository priceRepository;

    /**
     * Constructs the service with the required repositories.
     *
     * @param itemRepository  repository used to fetch all tracked items
     * @param priceRepository repository used to fetch the latest price per item
     */
    public InventoryService(ItemRepository itemRepository, PriceRepository priceRepository) {
        this.itemRepository = itemRepository;
        this.priceRepository = priceRepository;
    }

    /**
     * Returns all tracked items enriched with their latest price and P&L data.
     * Items with no price records or zero tracked quantity are excluded.
     *
     * @return list of InventoryItemView records sorted by total value descending
     */
    public List<InventoryItemView> getInventoryItems() {
        return itemRepository.findAll().stream()
                .filter(item -> item.getTrackedQuantity() > 0)
                .map(this::toView)
                .filter(view -> view != null)
                .sorted((a, b) -> Double.compare(b.totalValueEur(), a.totalValueEur()))
                .toList();
    }

    /**
     * Converts an Item entity to an InventoryItemView by joining with its latest price record.
     * Returns null if no price record exists for the item.
     *
     * @param item the Item entity to convert
     * @return an InventoryItemView with price and P&L data, or null if no price is available
     */
    private InventoryItemView toView(Item item) {
        Optional<Price> latestPrice = priceRepository.findTopByItemOrderByDateDesc(item);
        if (latestPrice.isEmpty()) return null;

        double price = latestPrice.get().getPriceEur();
        int quantity = item.getTrackedQuantity();
        double totalValue = price * quantity;
        double costBasisPerUnit = item.getCostBasisEur() / quantity;
        double pnlPct = costBasisPerUnit > 0
                ? ((price - costBasisPerUnit) / costBasisPerUnit) * 100
                : 0.0;

        return new InventoryItemView(
                item.getName(),
                item.getMarketHashName(),
                item.getIconUrl(),
                quantity,
                price,
                totalValue,
                costBasisPerUnit,
                Math.round(pnlPct * 100.0) / 100.0
        );
    }
}
