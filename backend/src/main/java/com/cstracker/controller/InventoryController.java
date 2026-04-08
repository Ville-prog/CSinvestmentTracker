/**
 * InventoryController.java
 *
 * REST controller exposing CS2 inventory and portfolio value endpoints.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.controller;

import com.cstracker.model.InventoryValue;
import com.cstracker.model.SteamItem;
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

    /**
     * Constructs the controller with the required Steam API service dependency.
     *
     * @param steamApiService service used to fetch CS2 inventory data from Steam
     */
    public InventoryController(SteamApiService steamApiService) {
        this.steamApiService = steamApiService;
    }

    /**
     * Returns all CS2 items in the given user's Steam inventory.
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
     * Returns the total estimated portfolio value with item breakdown.
     * GET /api/inventory/{steamId}/value
     *
     * @param steamId the 64-bit Steam ID of the target user
     * @return InventoryValue containing item count, total USD value, and item list
     */
    @GetMapping("/{steamId}/value")
    public InventoryValue getInventoryValue(@PathVariable String steamId) {
        List<SteamItem> items = steamApiService.getInventory(steamId);
        // TODO: replace with real prices from a CS2 price API
        double totalValueUsd = items.size() * 50.0;
        return new InventoryValue(steamId, items.size(), totalValueUsd, items);
    }
}
