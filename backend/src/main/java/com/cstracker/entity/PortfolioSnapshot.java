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

    @Column(name = "total_value_eur", nullable = false)
    private double totalValueEur;

    @Column(name = "item_count", nullable = false)
    private int itemCount;

    @Column(name = "total_cost_basis_eur")
    private Double totalCostBasisEur;

    /** @return the auto-generated database primary key */
    public Long getId() { return id; }

    /** @return the date this snapshot was recorded */
    public LocalDate getDate() { return date; }

    /** @param date the date this snapshot was recorded */
    public void setDate(LocalDate date) { this.date = date; }

    /** @return the estimated total market value of all tracked items in EUR */
    public double getTotalValueEur() { return totalValueEur; }

    /** @param totalValueEur the estimated total market value of all tracked items in EUR */
    public void setTotalValueEur(double totalValueEur) { this.totalValueEur = totalValueEur; }

    /** @return the number of items in the inventory on this date */
    public int getItemCount() { return itemCount; }

    /** @param itemCount the number of items in the inventory on this date */
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }

    /** @return the total cost basis in EUR for all marketable items at the time of acquisition, or 0 if not yet set */
    public double getTotalCostBasisEur() { return totalCostBasisEur != null ? totalCostBasisEur : 0.0; }

    /** @param totalCostBasisEur the total cost basis in EUR for all marketable items */
    public void setTotalCostBasisEur(double totalCostBasisEur) { this.totalCostBasisEur = totalCostBasisEur; }
}
