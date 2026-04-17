/**
 * ItemRepository.java
 *
 * Spring Data JPA repository for Item entities.
 * Provides standard CRUD operations and a query to look up items by market hash name.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.repository;

import com.cstracker.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Finds an item by its full market hash name including wear condition.
     *
     * @param marketHashName the market hash name to search for (e.g. "AK-47 | Redline (Field-Tested)")
     * @return an Optional containing the matching Item, or empty if not found
     */
    Optional<Item> findByMarketHashName(String marketHashName);

    /**
     * Finds all items whose tracked quantity exceeds the given threshold.
     * Used to gather the set of currently tracked items for daily pricing.
     *
     * @param threshold exclusive lower bound for tracked quantity (pass 0 for all tracked items)
     * @return list of items with trackedQuantity strictly greater than the threshold
     */
    List<Item> findByTrackedQuantityGreaterThan(int threshold);
}
