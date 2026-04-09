/**
 * MarketController.java
 *
 * REST controller exposing market data endpoints for external financial indices.
 * Currently provides historical S&P 500 daily close prices via Yahoo Finance.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.controller;

import com.cstracker.service.MarketDataService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarketDataService marketDataService;

    /**
     * Constructs the controller with the required market data service.
     *
     * @param marketDataService service used to fetch external market price data
     */
    public MarketController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    /**
     * Returns daily S&P 500 closing prices from the given date to today.
     * GET /api/market/sp500?from=YYYY-MM-DD
     *
     * @param from the start date (inclusive) for the price history query
     * @return list of maps each containing a date string and closing price
     */
    @GetMapping("/sp500")
    public List<Map<String, Object>> getSp500(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from) {
        return marketDataService.fetchSp500Since(from);
    }
}
