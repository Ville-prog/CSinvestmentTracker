/**
 * SteamApiService.java
 *
 * Service layer for fetching CS2 inventory data from the Steam community API.
 * Currently returns stub data; replace the body of getInventory() with a real HTTP call when ready.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.service;

import com.cstracker.model.SteamItem;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class SteamApiService {

    // Steam inventory is public — no API key required for this endpoint
    private static final String INVENTORY_URL =
            "https://steamcommunity.com/inventory/{steamId}/730/2?l=english&count=5000";

    private final RestTemplate restTemplate;

    /**
     * Constructs the service with the shared RestTemplate used for HTTP calls.
     *
     * @param restTemplate the RestTemplate bean injected by Spring
     */
    public SteamApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Retrieves all CS2 items from the specified user's Steam inventory.
     * Currently returns hardcoded stub data. Replace with a real Steam API call
     * using INVENTORY_URL once the response parsing logic is in place.
     *
     * @param steamId the 64-bit Steam ID of the target user
     * @return list of SteamItem records from the user's CS2 inventory
     */
    public List<SteamItem> getInventory(String steamId) {
        // TODO: Replace stub with real Steam API call
        //
        // String url = INVENTORY_URL.replace("{steamId}", steamId);
        // ResponseEntity<SteamInventoryResponse> response =
        //     restTemplate.getForEntity(url, SteamInventoryResponse.class);
        // return parseInventory(response.getBody());

        return List.of(
                new SteamItem("310777179", "AK-47 | Redline",
                        "AK-47 | Redline (Field-Tested)", "", true, true, 1),
                new SteamItem("175274955", "AWP | Dragon Lore",
                        "AWP | Dragon Lore (Factory New)", "", true, true, 1),
                new SteamItem("311346280", "M4A4 | Howl",
                        "M4A4 | Howl (Minimal Wear)", "", true, true, 1)
        );
    }
}
