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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
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

    /**
     * Fetches the most recent price record for each item in the given list in a single query,
     * avoiding the N+1 problem when loading the full inventory view.
     *
     * @param items the list of items to look up
     * @return list of the most recent Price record per item
     */
    @Query("SELECT p FROM Price p WHERE p.item IN :items AND p.date = (SELECT MAX(p2.date) FROM Price p2 WHERE p2.item = p.item)")
    List<Price> findLatestPricesForItems(@Param("items") List<Item> items);
}
