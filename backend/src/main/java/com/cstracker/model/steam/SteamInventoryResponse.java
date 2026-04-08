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

    public List<SteamAsset> getAssets() { return assets; }
    public void setAssets(List<SteamAsset> assets) { this.assets = assets; }

    public List<SteamDescription> getDescriptions() { return descriptions; }
    public void setDescriptions(List<SteamDescription> descriptions) { this.descriptions = descriptions; }

    public int getTotalInventoryCount() { return totalInventoryCount; }
    public void setTotalInventoryCount(int totalInventoryCount) { this.totalInventoryCount = totalInventoryCount; }
}
