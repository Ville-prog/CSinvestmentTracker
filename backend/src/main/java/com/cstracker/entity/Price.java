/**
 * Price.java
 *
 * JPA entity representing a daily price snapshot for a single CS2 item from the Steam Market.
 * Maps to the prices table with a unique constraint on (item_id, date) to prevent duplicate records.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * A daily price snapshot for a single CS2 item from the Steam Market.
 */
@Entity
@Table(name = "prices", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"item_id", "date"})
})
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "price_usd", nullable = false)
    private double priceUsd;

    /** @return the auto-generated database primary key */
    public Long getId() { return id; }

    /** @return the item this price record belongs to */
    public Item getItem() { return item; }

    /** @param item the item this price record belongs to */
    public void setItem(Item item) { this.item = item; }

    /** @return the date this price was recorded */
    public LocalDate getDate() { return date; }

    /** @param date the date this price was recorded */
    public void setDate(LocalDate date) { this.date = date; }

    /** @return the Steam Market median price in USD for this item on this date */
    public double getPriceUsd() { return priceUsd; }

    /** @param priceUsd the Steam Market median price in USD for this item on this date */
    public void setPriceUsd(double priceUsd) { this.priceUsd = priceUsd; }
}
