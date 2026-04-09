/**
 * PriceRepository.java
 *
 * Spring Data JPA repository for Price entities.
 * Provides queries for looking up daily price records and the most recent price for an item.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.repository;

import com.cstracker.entity.Item;
import com.cstracker.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PriceRepository extends JpaRepository<Price, Long> {

    /**
     * Finds the price record for a specific item on a specific date.
     *
     * @param item the item to look up
     * @param date the date to look up
     * @return an Optional containing the matching Price, or empty if not found
     */
    Optional<Price> findByItemAndDate(Item item, LocalDate date);

    /**
     * Finds the most recent price record for a given item.
     *
     * @param item the item to look up
     * @return an Optional containing the most recent Price, or empty if none exist
     */
    Optional<Price> findTopByItemOrderByDateDesc(Item item);
}
