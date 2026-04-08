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

    public Long getId() { return id; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public double getPriceUsd() { return priceUsd; }
    public void setPriceUsd(double priceUsd) { this.priceUsd = priceUsd; }
}
