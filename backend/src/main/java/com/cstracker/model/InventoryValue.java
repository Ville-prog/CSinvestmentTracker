/**
 * InventoryValue.java
 *
 * Immutable record representing the calculated total value of a user's CS2 skin portfolio.
 * Returned by the /api/inventory/{steamId}/value endpoint.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.model;

import java.util.List;

/**
 * The aggregated portfolio value for a single Steam user.
 *
 * @param steamId       the 64-bit Steam ID of the portfolio owner
 * @param itemCount     total number of CS2 items in the inventory
 * @param totalValueUsd estimated total market value of all items in USD
 * @param items         the full list of individual SteamItem records
 */
public record InventoryValue(
        String steamId,
        int itemCount,
        double totalValueUsd,
        List<SteamItem> items
) {}
