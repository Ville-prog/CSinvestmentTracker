package com.cstracker.model.steam;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps the response from the Steam Market price overview API.
 */
public class SteamPriceResponse {

    private boolean success;

    @JsonProperty("lowest_price")
    private String lowestPrice;

    @JsonProperty("median_price")
    private String medianPrice;

    private String volume;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getLowestPrice() { return lowestPrice; }
    public void setLowestPrice(String lowestPrice) { this.lowestPrice = lowestPrice; }

    public String getMedianPrice() { return medianPrice; }
    public void setMedianPrice(String medianPrice) { this.medianPrice = medianPrice; }

    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }
}
