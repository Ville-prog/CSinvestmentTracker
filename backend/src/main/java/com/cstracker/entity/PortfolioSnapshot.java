/**
 * PortfolioSnapshot.java
 *
 * JPA entity representing a daily snapshot of the total CS2 portfolio value.
 * Maps to the portfolio_snapshots table with a unique constraint on date to prevent duplicate records.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * A daily snapshot of the total CS2 portfolio value.
 * Used to build the portfolio value chart over time.
 */
@Entity
@Table(name = "portfolio_snapshots")
public class PortfolioSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Column(name = "total_value_usd", nullable = false)
    private double totalValueUsd;

    @Column(name = "item_count", nullable = false)
    private int itemCount;

    /** @return the auto-generated database primary key */
    public Long getId() { return id; }

    /** @return the date this snapshot was recorded */
    public LocalDate getDate() { return date; }

    /** @param date the date this snapshot was recorded */
    public void setDate(LocalDate date) { this.date = date; }

    /** @return the estimated total market value of all tracked items in USD */
    public double getTotalValueUsd() { return totalValueUsd; }

    /** @param totalValueUsd the estimated total market value of all tracked items in USD */
    public void setTotalValueUsd(double totalValueUsd) { this.totalValueUsd = totalValueUsd; }

    /** @return the number of items in the inventory on this date */
    public int getItemCount() { return itemCount; }

    /** @param itemCount the number of items in the inventory on this date */
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
}
