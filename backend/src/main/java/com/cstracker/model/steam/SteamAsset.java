/**
 * SteamAsset.java
 *
 * Data transfer object representing a single asset entry in the Steam inventory API response.
 * Assets are joined to SteamDescription objects by classid and instanceid to build full item records.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.model.steam;

/**
 * Represents a single asset entry from the Steam inventory API response.
 * Assets identify what items a user owns. Joined to SteamDescription by classid + instanceid.
 */
public class SteamAsset {

    private String classid;
    private String instanceid;
    private String amount;

    /** @return the Steam class ID identifying the item type */
    public String getClassid() { return classid; }

    /** @param classid the Steam class ID identifying the item type */
    public void setClassid(String classid) { this.classid = classid; }

    /** @return the Steam instance ID distinguishing item variants */
    public String getInstanceid() { return instanceid; }

    /** @param instanceid the Steam instance ID distinguishing item variants */
    public void setInstanceid(String instanceid) { this.instanceid = instanceid; }

    /** @return the quantity of this item in the inventory as a string */
    public String getAmount() { return amount; }

    /** @param amount the quantity of this item in the inventory as a string */
    public void setAmount(String amount) { this.amount = amount; }
}
