package com.cstracker.model.steam;

/**
 * Represents a single asset entry from the Steam inventory API response.
 * Assets identify what items a user owns. Joined to SteamDescription by classid + instanceid.
 */
public class SteamAsset {

    private String classid;
    private String instanceid;
    private String amount;

    public String getClassid() { return classid; }
    public void setClassid(String classid) { this.classid = classid; }

    public String getInstanceid() { return instanceid; }
    public void setInstanceid(String instanceid) { this.instanceid = instanceid; }

    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
}
