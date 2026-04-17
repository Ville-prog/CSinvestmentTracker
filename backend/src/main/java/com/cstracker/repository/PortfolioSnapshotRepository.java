/**
 * PortfolioSnapshotRepository.java
 *
 * Spring Data JPA repository for PortfolioSnapshot entities.
 * Provides queries for retrieving daily portfolio snapshots by date and in chronological order.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.repository;

import com.cstracker.entity.PortfolioSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, Long> {

    /**
     * Finds the portfolio snapshot for a specific date.
     *
     * @param date the date to look up
     * @return an Optional containing the matching snapshot, or empty if not found
     */
    Optional<PortfolioSnapshot> findByDate(LocalDate date);

    /**
     * Returns all portfolio snapshots ordered by date ascending.
     *
     * @return list of all snapshots from oldest to most recent
     */
    List<PortfolioSnapshot> findAllByOrderByDateAsc();

    /**
     * Returns the maximum item_count across all snapshots on or after the given date.
     * Used as the sanity-check baseline for today's Steam response: if today's
     * response size is far below this, the run is treated as degraded.
     *
     * @param since inclusive lower bound for the snapshot date window
     * @return the maximum observed item_count in the window, or null if no snapshots exist in the window
     */
    @Query("SELECT MAX(p.itemCount) FROM PortfolioSnapshot p WHERE p.date >= :since")
    Integer findMaxItemCountSince(@Param("since") LocalDate since);
}
