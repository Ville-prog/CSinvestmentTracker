package com.cstracker.controller;

import com.cstracker.entity.PortfolioSnapshot;
import com.cstracker.repository.PortfolioSnapshotRepository;
import com.cstracker.service.PriceCollectionJob;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioSnapshotRepository snapshotRepository;
    private final PriceCollectionJob priceCollectionJob;

    @Value("${admin.secret}")
    private String adminSecret;

    public PortfolioController(PortfolioSnapshotRepository snapshotRepository, PriceCollectionJob priceCollectionJob) {
        this.snapshotRepository = snapshotRepository;
        this.priceCollectionJob = priceCollectionJob;
    }

    // Returns all daily portfolio snapshots ordered by date ascending
    @GetMapping("/history")
    public List<PortfolioSnapshot> getHistory() {
        return snapshotRepository.findAllByOrderByDateAsc();
    }

    // Returns the most recent portfolio snapshot
    @GetMapping("/latest")
    public PortfolioSnapshot getLatest() {
        List<PortfolioSnapshot> all = snapshotRepository.findAllByOrderByDateAsc();
        return all.isEmpty() ? null : all.get(all.size() - 1);
    }

    // Manually triggers the price collection job — protected by secret header
    @PostMapping("/trigger-collection")
    public String triggerCollection(@RequestHeader("X-Admin-Secret") String secret) {
        if (!adminSecret.equals(secret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        priceCollectionJob.collectPrices();
        return "Price collection triggered";
    }
}
