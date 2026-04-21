/**
 * AdminController.java
 *
 * Admin endpoints for manually triggering price collection when the nightly Steam API call fails.
 * Accepts a raw Steam inventory JSON body so the user can supply inventory data fetched from
 * a browser session and bypass the server-side Steam call entirely.
 *
 * @author Ville Laaksoaho
 * Dependencies: PriceCollectionJob.java, SteamApiService.java
 */
package com.cstracker.controller;

import com.cstracker.model.SteamItem;
import com.cstracker.model.steam.SteamInventoryResponse;
import com.cstracker.service.PriceCollectionJob;
import com.cstracker.service.SteamApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final SteamApiService steamApiService;
    private final PriceCollectionJob priceCollectionJob;

    /**
     * Constructs the controller with required service dependencies.
     *
     * @param steamApiService    used to parse the raw Steam inventory JSON into SteamItem records
     * @param priceCollectionJob used to run the collection logic with the parsed inventory
     */
    public AdminController(SteamApiService steamApiService, PriceCollectionJob priceCollectionJob) {
        this.steamApiService = steamApiService;
        this.priceCollectionJob = priceCollectionJob;
    }

    /**
     * Accepts a raw Steam inventory JSON body, parses it, and runs the full price collection
     * as if the nightly job had fetched the inventory itself.
     * POST /api/admin/collect-from-inventory
     *
     * @param inventoryResponse the raw Steam community inventory API response body
     * @return 200 with a summary message, or 400 if the inventory could not be parsed
     */
    @PostMapping("/collect-from-inventory")
    public ResponseEntity<String> collectFromInventory(@RequestBody SteamInventoryResponse inventoryResponse) {
        List<SteamItem> items = steamApiService.parseInventory(inventoryResponse);
        log.info("Manual collect-from-inventory received {} items after parsing", items.size());

        if (items.isEmpty()) {
            return ResponseEntity.badRequest().body("Inventory parsed to 0 items — check the JSON body.");
        }

        priceCollectionJob.collectFromManualInventory(items);
        return ResponseEntity.ok("Collection started with " + items.size() + " items.");
    }
}
