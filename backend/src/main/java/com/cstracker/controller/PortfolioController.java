/**
 * PortfolioController.java
 *
 * REST controller exposing portfolio snapshot history and a protected manual trigger endpoint.
 * Snapshots are written by the nightly PriceCollectionJob and read here by the frontend.
 *
 * @author Ville Laaksoaho
 */
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

    /**
     * Constructs the controller with the required repository and job dependencies.
     *
     * @param snapshotRepository repository used to read portfolio snapshot history
     * @param priceCollectionJob the nightly job that can be manually triggered
     */
    public PortfolioController(PortfolioSnapshotRepository snapshotRepository, PriceCollectionJob priceCollectionJob) {
        this.snapshotRepository = snapshotRepository;
        this.priceCollectionJob = priceCollectionJob;
    }

    /**
     * Returns all daily portfolio snapshots ordered by date ascending.
     * GET /api/portfolio/history
     *
     * @return list of all portfolio snapshots from oldest to most recent
     */
    @GetMapping("/history")
    public List<PortfolioSnapshot> getHistory() {
        return snapshotRepository.findAllByOrderByDateAsc();
    }

    /**
     * Returns the most recent portfolio snapshot.
     * GET /api/portfolio/latest
     *
     * @return the latest portfolio snapshot, or null if no snapshots exist
     */
    @GetMapping("/latest")
    public PortfolioSnapshot getLatest() {
        List<PortfolioSnapshot> all = snapshotRepository.findAllByOrderByDateAsc();
        return all.isEmpty() ? null : all.get(all.size() - 1);
    }

    /**
     * Manually triggers the price collection job asynchronously.
     * POST /api/portfolio/trigger-collection
     * Requires a valid X-Admin-Secret header matching the ADMIN_SECRET environment variable.
     *
     * @param secret the admin secret from the X-Admin-Secret request header
     * @return confirmation message if authorised
     */
    @PostMapping("/trigger-collection")
    public String triggerCollection(@RequestHeader("X-Admin-Secret") String secret) {
        if (!adminSecret.equals(secret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        new Thread(priceCollectionJob::collectPrices).start();
        return "Price collection triggered";
    }
}
