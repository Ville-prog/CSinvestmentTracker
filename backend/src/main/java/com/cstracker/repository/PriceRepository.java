package com.cstracker.repository;

import com.cstracker.entity.Item;
import com.cstracker.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PriceRepository extends JpaRepository<Price, Long> {
    Optional<Price> findByItemAndDate(Item item, LocalDate date);
    Optional<Price> findTopByItemOrderByDateDesc(Item item);
}
