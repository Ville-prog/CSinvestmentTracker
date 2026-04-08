/**
 * InventoryController.java
 *
 * REST controller exposing CS2 inventory and portfolio value endpoints.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.controller;

import com.cstracker.model.InventoryValue;
import com.cstracker.model.PricedItem;
import com.cstracker.model.SteamItem;
import com.cstracker.service.PriceService;
import com.cstracker.service.SteamApiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final SteamApiService steamApiService;
    private final PriceService priceService;

    /**
     * Constructs the controller with required service dependencies.
     *
     * @param steamApiService service used to fetch CS2 inventory data from Steam
     * @param priceService    service used to fetch Steam Market prices
     */
    public InventoryController(SteamApiService steamApiService, PriceService priceService) {
        this.steamApiService = steamApiService;
        this.priceService = priceService;
    }

    /**
     * Returns all CS2 items in the given user's Steam inventory without prices.
     * GET /api/inventory/{steamId}
     *
     * @param steamId the 64-bit Steam ID of the target user
     * @return list of SteamItem records
     */
    @GetMapping("/{steamId}")
    public List<SteamItem> getInventory(@PathVariable String steamId) {
        return steamApiService.getInventory(steamId);
    }

    /**
     * Returns the total portfolio value with per-item Steam Market prices.
     * GET /api/inventory/{steamId}/value
     *
     * @param steamId the 64-bit Steam ID of the target user
     * @return InventoryValue with item count, total USD value, and priced item list
     */
    @GetMapping("/{steamId}/value")
    public InventoryValue getInventoryValue(@PathVariable String steamId) {
        List<SteamItem> items = steamApiService.getInventory(steamId);
        List<PricedItem> pricedItems = priceService.addPrices(items);
        double totalValueUsd = pricedItems.stream().mapToDouble(PricedItem::totalValueUsd).sum();
        return new InventoryValue(steamId, items.size(), totalValueUsd, pricedItems);
    }
}
