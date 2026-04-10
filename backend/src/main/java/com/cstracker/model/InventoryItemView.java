/**
 * InventoryItemView.java
 *
 * Response model representing a single tracked CS2 item with its latest price and P&L data.
 * Returned by the GET /api/inventory/items endpoint for use in the frontend inventory table.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.model;

/**
 * A tracked CS2 item enriched with latest price, total value, and profit/loss data.
 *
 * @param name             display name of the item
 * @param marketHashName   full market name including wear condition
 * @param iconUrl          Steam CDN icon URL for the item image
 * @param quantity         number of units currently tracked
 * @param priceEur         latest known Steam Market price per unit in EUR
 * @param totalValueEur    priceEur multiplied by quantity
 * @param costBasisPerUnit average cost basis per unit in EUR (costBasisEur / trackedQuantity)
 * @param pnlPct           profit/loss percentage relative to cost basis
 */
public record InventoryItemView(
        String name,
        String marketHashName,
        String iconUrl,
        int quantity,
        double priceEur,
        double totalValueEur,
        double costBasisPerUnit,
        double pnlPct
) {}
