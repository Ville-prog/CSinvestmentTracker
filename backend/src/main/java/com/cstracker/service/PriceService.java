/**
 * PriceService.java
 *
 * Fetches CS2 item prices from the Steam Community Market price overview API.
 * Returns median prices in EUR. Items with no price data return 0.0.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.service;

import com.cstracker.model.PricedItem;
import com.cstracker.model.SteamItem;
import com.cstracker.model.steam.SteamPriceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class PriceService {

    private static final Logger log = LoggerFactory.getLogger(PriceService.class);

    // currency=3 = EUR
    private static final String PRICE_URL =
            "https://steamcommunity.com/market/priceoverview/?appid=730&currency=3&market_hash_name=%s";

    private final RestTemplate restTemplate;

    /**
     * Constructs the service with the shared RestTemplate.
     *
     * @param restTemplate the RestTemplate bean injected by Spring
     */
    public PriceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Enriches a list of SteamItems with current Steam Market median prices.
     * Items that are not marketable or have no price data are returned with priceEur=0.
     *
     * @param items list of SteamItem records from the inventory
     * @return list of PricedItem records with price and total value fields populated
     */
    public List<PricedItem> addPrices(List<SteamItem> items) {
        return items.stream()
                .map(item -> {
                    double price = item.marketable() ? fetchPrice(item.marketHashName()) : 0.0;
                    return new PricedItem(
                            item.classId(),
                            item.name(),
                            item.marketHashName(),
                            item.iconUrl(),
                            item.tradable(),
                            item.marketable(),
                            item.amount(),
                            price,
                            price * item.amount()
                    );
                })
                .toList();
    }

    /**
     * Fetches the Steam Market median price in EUR for a single item.
     * Returns 0.0 if the request fails or the item has no price data.
     *
     * @param marketHashName the full market name of the item including wear
     * @return median price in EUR, or 0.0 on failure
     */
    double fetchPrice(String marketHashName) {
        try {
            String encoded = URLEncoder.encode(marketHashName, StandardCharsets.UTF_8);
            String url = String.format(PRICE_URL, encoded);
            SteamPriceResponse response = restTemplate.getForObject(url, SteamPriceResponse.class);

            if (response == null || !response.isSuccess()) {
                log.warn("No price data for '{}'", marketHashName);
                return 0.0;
            }

            String priceStr = response.getMedianPrice() != null
                    ? response.getMedianPrice()
                    : response.getLowestPrice();

            return parsePrice(priceStr);

        } catch (Exception e) {
            log.warn("Failed to fetch price for '{}': {}", marketHashName, e.getMessage());
            return 0.0;
        }
    }

    /**
     * Parses a Steam Market price string in EUR format (e.g. "1.234,56€") into a double.
     * Handles European comma-as-decimal and period-as-thousands-separator formatting.
     *
     * @param priceStr price string from the Steam Market API
     * @return parsed price as a double, or 0.0 if parsing fails
     */
    private double parsePrice(String priceStr) {
        if (priceStr == null || priceStr.isBlank()) return 0.0;
        try {
            String cleaned = priceStr
                    .replaceAll("[€$\\s]", "")
                    .replace("--", "00")
                    .replace(".", "")
                    .replace(",", ".");
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            log.warn("Could not parse price string: '{}'", priceStr);
            return 0.0;
        }
    }
}
