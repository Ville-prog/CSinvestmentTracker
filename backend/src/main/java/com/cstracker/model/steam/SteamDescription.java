package com.cstracker.model.steam;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an item description from the Steam inventory API response.
 * Contains display data joined to SteamAsset entries by classid + instanceid.
 */
public class SteamDescription {

    private String classid;
    private String instanceid;
    private String name;

    @JsonProperty("market_hash_name")
    private String marketHashName;

    @JsonProperty("icon_url")
    private String iconUrl;

    private int tradable;
    private int marketable;

    public String getClassid() { return classid; }
    public void setClassid(String classid) { this.classid = classid; }

    public String getInstanceid() { return instanceid; }
    public void setInstanceid(String instanceid) { this.instanceid = instanceid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMarketHashName() { return marketHashName; }
    public void setMarketHashName(String marketHashName) { this.marketHashName = marketHashName; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public int getTradable() { return tradable; }
    public void setTradable(int tradable) { this.tradable = tradable; }

    public int getMarketable() { return marketable; }
    public void setMarketable(int marketable) { this.marketable = marketable; }
}
