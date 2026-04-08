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

    public Long getId() { return id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public double getTotalValueUsd() { return totalValueUsd; }
    public void setTotalValueUsd(double totalValueUsd) { this.totalValueUsd = totalValueUsd; }

    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
}
