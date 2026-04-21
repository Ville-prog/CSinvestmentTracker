/**
 * SteamApiService.java
 *
 * Service layer for fetching CS2 inventory data from the Steam community inventory endpoint.
 * Joins Steam asset and description arrays into a flat list of SteamItem records.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.service;

import com.cstracker.model.SteamItem;
import com.cstracker.model.steam.SteamDescription;
import com.cstracker.model.steam.SteamInventoryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SteamApiService {

    private static final Logger log = LoggerFactory.getLogger(SteamApiService.class);

    @Value("${steam.inventory.url}")
    private String inventoryUrl;

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
     * Fetches all CS2 items from the given user's public Steam inventory.
     * Returns an empty list if the profile is private or the inventory is empty.
     *
     * @param steamId the 64-bit Steam ID of the target user
     * @return list of SteamItem records from the user's CS2 inventory
     */
    public List<SteamItem> getInventory(String steamId) {
        String url = String.format(inventoryUrl, steamId);

        try {
            log.info("Fetching inventory for steamId={}", steamId);
            SteamInventoryResponse response = restTemplate.getForObject(url, SteamInventoryResponse.class);

            if (response == null || response.getAssets() == null || response.getDescriptions() == null) {
                log.warn("Empty or inaccessible inventory for steamId={}", steamId);
                return Collections.emptyList();
            }

            log.info("Got {} assets and {} descriptions for steamId={}",
                    response.getAssets().size(), response.getDescriptions().size(), steamId);
            return parseInventory(response);

        } catch (HttpClientErrorException e) {
            log.error("Steam returned {} for steamId={}", e.getStatusCode(), steamId);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching inventory for steamId={}: {}", steamId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Joins the assets and descriptions arrays from the Steam API response into
     * a flat list of SteamItem records. Assets are grouped by classid+instanceid
     * so duplicate items are represented as a single entry with a combined amount.
     *
     * @param response the raw Steam inventory API response
     * @return list of SteamItem records with ownership and display data merged
     */
    public List<SteamItem> parseInventory(SteamInventoryResponse response) {
        Map<String, SteamDescription> descriptionMap = response.getDescriptions().stream()
                .collect(Collectors.toMap(
                        d -> d.getClassid() + "_" + d.getInstanceid(),
                        d -> d,
                        (existing, duplicate) -> existing
                ));

        Map<String, Integer> amountByKey = response.getAssets().stream()
                .collect(Collectors.groupingBy(
                        a -> a.getClassid() + "_" + a.getInstanceid(),
                        Collectors.summingInt(a -> Integer.parseInt(a.getAmount()))
                ));

        return amountByKey.entrySet().stream()
                .map(entry -> {
                    SteamDescription desc = descriptionMap.get(entry.getKey());
                    if (desc == null) return null;
                    return new SteamItem(
                            desc.getClassid(),
                            desc.getName(),
                            desc.getMarketHashName(),
                            desc.getIconUrl(),
                            desc.getTradable() == 1,
                            desc.getMarketable() == 1,
                            entry.getValue()
                    );
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());
    }
}
