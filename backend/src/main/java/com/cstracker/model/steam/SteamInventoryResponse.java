/**
 * SteamInventoryResponse.java
 *
 * Data transfer object mapping the top-level JSON response from the Steam community inventory endpoint.
 * Contains parallel arrays of assets and descriptions that are joined by classid and instanceid.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.model.steam;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Maps the top-level JSON response from the Steam community inventory endpoint.
 * Assets and descriptions are at the top level with no wrapper object.
 */
public class SteamInventoryResponse {

    private List<SteamAsset> assets;
    private List<SteamDescription> descriptions;

    @JsonProperty("total_inventory_count")
    private int totalInventoryCount;

    /** @return list of asset entries representing items owned by the user */
    public List<SteamAsset> getAssets() { return assets; }

    /** @param assets list of asset entries representing items owned by the user */
    public void setAssets(List<SteamAsset> assets) { this.assets = assets; }

    /** @return list of description entries containing display and market metadata */
    public List<SteamDescription> getDescriptions() { return descriptions; }

    /** @param descriptions list of description entries containing display and market metadata */
    public void setDescriptions(List<SteamDescription> descriptions) { this.descriptions = descriptions; }

    /** @return total number of items in the inventory as reported by Steam */
    public int getTotalInventoryCount() { return totalInventoryCount; }

    /** @param totalInventoryCount total number of items in the inventory as reported by Steam */
    public void setTotalInventoryCount(int totalInventoryCount) { this.totalInventoryCount = totalInventoryCount; }
}
