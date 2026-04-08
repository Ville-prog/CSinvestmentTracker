package com.cstracker.repository;

import com.cstracker.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByMarketHashName(String marketHashName);
}
