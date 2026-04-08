/**
 * SteamItem.java
 *
 * Immutable record representing a single CS2 skin item retrieved from a Steam inventory.
 * Combines asset and description data from the Steam inventory API into a flat structure.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.model;

/**
 * A CS2 skin item from a Steam inventory.
 *
 * @param classId        Steam class ID that identifies the item type
 * @param name           display name of the skin (e.g. "AK-47 | Redline")
 * @param marketHashName full market name including wear (e.g. "AK-47 | Redline (Field-Tested)")
 * @param iconUrl        URL of the item's icon image on the Steam CDN
 * @param tradable       whether the item can be traded with other users
 * @param marketable     whether the item can be listed on the Steam Community Market
 * @param amount         quantity of this item in the inventory
 */
public record SteamItem(
        String classId,
        String name,
        String marketHashName,
        String iconUrl,
        boolean tradable,
        boolean marketable,
        int amount
) {}
