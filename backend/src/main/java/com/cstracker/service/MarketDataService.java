package com.cstracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    public MarketDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Map<String, Object>> fetchSp500Since(LocalDate from) {
        long period1 = from.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        long period2 = LocalDate.now().plusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        String url = String.format(YAHOO_URL, period1, period2);

        List<Map<String, Object>> result = new ArrayList<>();

        try {
            Map response = restTemplate.getForObject(url, Map.class);
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
