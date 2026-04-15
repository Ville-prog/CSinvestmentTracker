/**
 * PriceServiceTest.java
 *
 * Unit tests for PriceService EUR price parsing and fetch fallback logic.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.service;

import com.cstracker.model.steam.SteamPriceResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    PriceService priceService;

    private SteamPriceResponse successResponse(String medianPrice) {
        SteamPriceResponse r = new SteamPriceResponse();
        r.setSuccess(true);
        r.setMedianPrice(medianPrice);
        return r;
    }

    private SteamPriceResponse lowestOnlyResponse(String lowestPrice) {
        SteamPriceResponse r = new SteamPriceResponse();
        r.setSuccess(true);
        r.setLowestPrice(lowestPrice);
        return r;
    }

    private void mockExchange(SteamPriceResponse response) {
        when(restTemplate.exchange(any(), eq(HttpMethod.GET), any(), eq(SteamPriceResponse.class)))
                .thenReturn(ResponseEntity.ok(response));
    }

    @Test
    void parsesStandardEurFormat() {
        mockExchange(successResponse("1,23€"));
        assertEquals(1.23, priceService.fetchPrice("AK-47 | Redline (Field-Tested)"), 0.001);
    }

    @Test
    void parsesThousandsSeparator() {
        mockExchange(successResponse("1.234,56€"));
        assertEquals(1234.56, priceService.fetchPrice("Some Expensive Skin"), 0.001);
    }

    @Test
    void parsesEurFormatWithSpace() {
        mockExchange(successResponse("12,34 €"));
        assertEquals(12.34, priceService.fetchPrice("Some Skin"), 0.001);
    }

    @Test
    void fallsBackToLowestPriceWhenMedianIsNull() {
        mockExchange(lowestOnlyResponse("5,00€"));
        assertEquals(5.0, priceService.fetchPrice("Some Skin"), 0.001);
    }

    @Test
    void returnsZeroWhenSuccessIsFalse() {
        SteamPriceResponse r = new SteamPriceResponse();
        r.setSuccess(false);
        mockExchange(r);
        assertEquals(0.0, priceService.fetchPrice("Some Skin"), 0.001);
    }

    @Test
    void returnsZeroWhenResponseIsNull() {
        when(restTemplate.exchange(any(), eq(HttpMethod.GET), any(), eq(SteamPriceResponse.class)))
                .thenReturn(ResponseEntity.ok(null));
        assertEquals(0.0, priceService.fetchPrice("Some Skin"), 0.001);
    }

    @Test
    void returnsZeroWhenRestTemplatethrows() {
        when(restTemplate.exchange(any(), eq(HttpMethod.GET), any(), eq(SteamPriceResponse.class)))
                .thenThrow(new RuntimeException("connection refused"));
        assertEquals(0.0, priceService.fetchPrice("Some Skin"), 0.001);
    }
}
