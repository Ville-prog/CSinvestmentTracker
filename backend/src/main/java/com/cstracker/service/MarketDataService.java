/**
 * MarketDataService.java
 *
 * Service for fetching historical S&P 500 price data from the Yahoo Finance API.
 * Returns daily closing prices as a list of date/close maps for use in portfolio comparison charts.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MarketDataService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataService.class);
    private static final String YAHOO_URL =
            "https://query1.finance.yahoo.com/v8/finance/chart/%%5EGSPC?interval=1d&period1=%d&period2=%d";

    private final RestTemplate restTemplate;

    /**
     * Constructs the service with the shared RestTemplate used for HTTP calls.
     *
     * @param restTemplate the RestTemplate bean injected by Spring
     */
    public MarketDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches daily S&P 500 closing prices from Yahoo Finance between the given start date and today.
     * Returns an empty list if the request fails or the response contains no data.
     *
     * @param from the start date (inclusive) for the historical price query
     * @return list of maps each containing "date" (ISO string) and "close" (double) entries
     */
    public List<Map<String, Object>> fetchSp500Since(LocalDate from) {
        long period1 = from.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        long period2 = LocalDate.now().plusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        String url = String.format(YAHOO_URL, period1, period2);

        List<Map<String, Object>> result = new ArrayList<>();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0 Safari/537.36");
            headers.set("Accept", "application/json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map response = responseEntity.getBody();
            Map chart = (Map) response.get("chart");
            List resultList = (List) chart.get("result");
            if (resultList == null || resultList.isEmpty()) return result;

            Map data = (Map) resultList.get(0);
            List<Long> timestamps = (List<Long>) data.get("timestamp");
            Map indicators = (Map) data.get("indicators");
            List quotes = (List) indicators.get("quote");
            Map quote = (Map) quotes.get(0);
            List<Double> closes = (List<Double>) quote.get("close");

            for (int i = 0; i < timestamps.size(); i++) {
                Double close = closes.get(i);
                if (close == null) continue;
                LocalDate date = Instant.ofEpochSecond(timestamps.get(i))
                        .atZone(ZoneOffset.UTC).toLocalDate();
                result.add(Map.of("date", date.toString(), "close", close));
            }
        } catch (Exception e) {
            log.error("Failed to fetch S&P 500 data: {}", e.getMessage());
        }

        return result;
    }
}
