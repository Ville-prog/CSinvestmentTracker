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
}
