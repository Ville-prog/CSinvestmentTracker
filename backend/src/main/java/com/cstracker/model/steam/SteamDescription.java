/**
 * SteamDescription.java
 *
 * Data transfer object representing an item description from the Steam inventory API response.
 * Contains display and market metadata joined to SteamAsset entries by classid and instanceid.
 *
 * @author Ville Laaksoaho
 */
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

    /** @return the Steam class ID identifying the item type */
    public String getClassid() { return classid; }

    /** @param classid the Steam class ID identifying the item type */
    public void setClassid(String classid) { this.classid = classid; }

    /** @return the Steam instance ID distinguishing item variants */
    public String getInstanceid() { return instanceid; }

    /** @param instanceid the Steam instance ID distinguishing item variants */
    public void setInstanceid(String instanceid) { this.instanceid = instanceid; }

    /** @return the display name of the item (e.g. "AK-47 | Redline") */
    public String getName() { return name; }

    /** @param name the display name of the item */
    public void setName(String name) { this.name = name; }

    /** @return the full market hash name including wear (e.g. "AK-47 | Redline (Field-Tested)") */
    public String getMarketHashName() { return marketHashName; }

    /** @param marketHashName the full market hash name including wear */
    public void setMarketHashName(String marketHashName) { this.marketHashName = marketHashName; }

    /** @return the Steam CDN icon URL for the item */
    public String getIconUrl() { return iconUrl; }

    /** @param iconUrl the Steam CDN icon URL for the item */
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    /** @return 1 if the item can be traded, 0 otherwise */
    public int getTradable() { return tradable; }

    /** @param tradable 1 if the item can be traded, 0 otherwise */
    public void setTradable(int tradable) { this.tradable = tradable; }

    /** @return 1 if the item can be listed on the Steam Market, 0 otherwise */
    public int getMarketable() { return marketable; }

    /** @param marketable 1 if the item can be listed on the Steam Market, 0 otherwise */
    public void setMarketable(int marketable) { this.marketable = marketable; }
}
