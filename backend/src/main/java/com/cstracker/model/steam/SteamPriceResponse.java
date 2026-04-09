/**
 * SteamPriceResponse.java
 *
 * Data transfer object mapping the JSON response from the Steam Market price overview API.
 * Contains the lowest price, median price, and volume for a single market listing.
 *
 * @author Ville Laaksoaho
 */
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

    /** @return true if the API call was successful */
    public boolean isSuccess() { return success; }

    /** @param success true if the API call was successful */
    public void setSuccess(boolean success) { this.success = success; }

    /** @return the current lowest listing price as a formatted string (e.g. "$1.23") */
    public String getLowestPrice() { return lowestPrice; }

    /** @param lowestPrice the current lowest listing price as a formatted string */
    public void setLowestPrice(String lowestPrice) { this.lowestPrice = lowestPrice; }

    /** @return the median sale price as a formatted string (e.g. "$1.23") */
    public String getMedianPrice() { return medianPrice; }

    /** @param medianPrice the median sale price as a formatted string */
    public void setMedianPrice(String medianPrice) { this.medianPrice = medianPrice; }

    /** @return the number of recent sales as a formatted string */
    public String getVolume() { return volume; }

    /** @param volume the number of recent sales as a formatted string */
    public void setVolume(String volume) { this.volume = volume; }
}
