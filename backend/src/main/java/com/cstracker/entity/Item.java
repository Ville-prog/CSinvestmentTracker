/**
 * Item.java
 *
 * JPA entity representing a unique CS2 skin item tracked in the system.
 * Maps to the items table and is referenced by Price records for daily price history.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.entity;

import jakarta.persistence.*;

/**
 * Represents a unique CS2 skin item tracked in the system.
 * Items are identified by their market hash name which includes wear condition.
 */
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_id")
    private String classId;

    @Column(name = "market_hash_name", unique = true, nullable = false)
    private String marketHashName;

    @Column(name = "name")
    private String name;

    @Column(name = "icon_url", length = 1024)
    private String iconUrl;

    /** @return the auto-generated database primary key */
    public Long getId() { return id; }

    /** @return the Steam class ID identifying this item type */
    public String getClassId() { return classId; }

    /** @param classId the Steam class ID identifying this item type */
    public void setClassId(String classId) { this.classId = classId; }

    /** @return the full market hash name including wear (e.g. "AK-47 | Redline (Field-Tested)") */
    public String getMarketHashName() { return marketHashName; }

    /** @param marketHashName the full market hash name including wear */
    public void setMarketHashName(String marketHashName) { this.marketHashName = marketHashName; }

    /** @return the display name of the item */
    public String getName() { return name; }

    /** @param name the display name of the item */
    public void setName(String name) { this.name = name; }

    /** @return the Steam CDN icon URL for the item */
    public String getIconUrl() { return iconUrl; }

    /** @param iconUrl the Steam CDN icon URL for the item */
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
}
