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

    public Long getId() { return id; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getMarketHashName() { return marketHashName; }
    public void setMarketHashName(String marketHashName) { this.marketHashName = marketHashName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
}
