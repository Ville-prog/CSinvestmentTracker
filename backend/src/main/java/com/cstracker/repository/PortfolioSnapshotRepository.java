package com.cstracker.repository;

import com.cstracker.entity.PortfolioSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, Long> {
    Optional<PortfolioSnapshot> findByDate(LocalDate date);
    List<PortfolioSnapshot> findAllByOrderByDateAsc();
}
