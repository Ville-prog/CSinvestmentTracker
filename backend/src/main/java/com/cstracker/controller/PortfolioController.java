package com.cstracker.controller;

import com.cstracker.entity.PortfolioSnapshot;
import com.cstracker.repository.PortfolioSnapshotRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioSnapshotRepository snapshotRepository;

    public PortfolioController(PortfolioSnapshotRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
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
}
