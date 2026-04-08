package com.cstracker.model;

/**
 * A CS2 inventory item enriched with current Steam Market price data.
 *
 * @param classId        Steam class ID identifying the item type
 * @param name           display name of the skin
 * @param marketHashName full market name including wear
 * @param iconUrl        Steam CDN icon URL
 * @param tradable       whether the item can be traded
 * @param marketable     whether the item can be listed on the market
 * @param amount         quantity owned
 * @param priceUsd       current Steam Market median price in USD per unit
 * @param totalValueUsd  priceUsd multiplied by amount
 */
public record PricedItem(
        String classId,
        String name,
        String marketHashName,
        String iconUrl,
        boolean tradable,
        boolean marketable,
        int amount,
        double priceUsd,
        double totalValueUsd
) {}
