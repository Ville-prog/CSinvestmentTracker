# CS Investment Tracker

A personal CS2 skin investment tracker that monitors portfolio value over time and compares it against the S&P 500.

## Live Demo

[csinvestmenttracker.vercel.app](https://csinvestmenttracker.vercel.app/)

## What is the CS2 skin market?

CS2 (Counter-Strike 2) is a free-to-play competetive shooter made by Valve with an **in-game economy** where players own cosmetic weapon skins. These skins are stored on your Steam account and can be freely traded, bought, and sold. Unlike most in-game items, **CS2 skins have real monetary value.**

**Converting skins to money:**
- **Steam balance:** Skins sold on the Steam Market convert to Steam wallet funds, which can be used to purchase games, hardware like the Steam Deck, or other items
- **Third-party platforms:** Sites like Skinport, CS.Money, and others allow users to sell skins for real money transferred directly to a bank account, PayPal, or crypto

**The market as an investment:**
The CS2 skin market has been active since 2013 and has grown into a **multi-billion dollar economy.** The overall market has historically shown steady appreciation over long time horizons, **with some analyses placing its performance on par with or above the S&P 500** over 5-10 year periods.

That said, individual items can be volatile. **The market behaves more like an index** when looked at in aggregate, smoothing out the swings of individual skins. **This tracker takes that view, measuring total portfolio value rather than betting on single items.**

## How it works

A nightly job runs at 11 PM UTC and is split into two stages. First, it fetches the Steam inventory and upserts any newly discovered items into the database. Second, it collects the current Steam Market price for **every tracked item in the database**, not only the items returned by today's Steam response. Prices are saved daily, building a historical record over time, and a portfolio snapshot (total value, total cost basis, unit count) is saved at the end of each run.

This two-stage design absorbs transient gaps in Steam's inventory response. If Steam temporarily returns fewer items than expected (truncation, rate limits, or silent filtering), tracked items still get priced from the DB and the portfolio value stays accurate.

### Detecting traded-away items

Because pricing is driven by the DB rather than the Steam response, the app needs a separate way to know when an item has actually been traded away. Each item carries a `last_seen_in_steam` timestamp that is advanced every time the item appears in a sane Steam response. Items whose last-seen timestamp falls outside a 7-day grace window are considered traded away and their tracked quantity is set to zero. A sanity gate further guards the process: if today's Steam response is below 50% of the recent 7-day maximum unit count, the run is treated as degraded — prices are still fetched, but `last_seen` and `trackedQuantity` are not touched so one bad Steam day cannot falsely age out the entire inventory.

## Pages

### Dashboard Page

Tracks total portfolio value trough two different charts:

**1. P&L %:** CS2 portfolio profit/loss as a percentage. The S&P 500 line can be toggled on or off for comparison. Calculated as profit/loss relative to cost basis:

```
P&L % = (current value - cost basis) / cost basis * 100
```

When the tracked quantity of an existing item increases (new units acquired), those new units are added to the cost basis at today's market price. When it decreases (units sold), the cost basis is scaled proportionally. Items seen for the very first time default to a cost basis of zero — today's price is not a valid proxy for the original acquisition cost of a pre-existing stack, so the initial cost basis must be set manually in the database. **This means the chart only moves when prices change; adding new items to the inventory does not affect the P&L line.** This mirrors how real investment portfolio trackers work: buying new shares increases your portfolio value, but does not count as a gain. Only price appreciation of what you already hold moves the return percentage.

**2. Total Value:** Raw portfolio value in EUR over time. Unlike the P&L chart, this reflects absolute value including the effect of adding new items to the inventory.

### Inventory Page

A sortable table of all tracked items showing name, item icon, quantity, price per unit, total value, and P&L % per item. Sortable by any column.

### About Page

A short summary of the project.

## Steam API limitations

This app currently only works for a single, hardcoded Steam inventory (mine). This is a deliberate design choice due to Steam API restrictions:

- The Steam community inventory endpoint is public but heavily rate limited
- There is no official API for real-time CS2 market prices
- Many third-party sites bypass these limits by running large networks of Steam bot accounts that scrape data continuously, which violates Steam's Terms of Service

This app takes the compliant approach: a nightly price collection job that respects rate limits (one request every 4 seconds). This works well in practice because CS2 skin prices don't fluctuate significantly day-to-day, making daily snapshots sufficient for investment tracking.

## Stack

- **Backend:** Java 21 / Spring Boot 3, deployed on Railway via Docker
- **Database:** PostgreSQL, managed trough Railway
- **Frontend:** React with Recharts, deployed on Vercel
- **External APIs:** Steam Community Market (prices), Yahoo Finance (S&P 500 history)

## Structure

```
CSinvestmentTracker/
├── backend/
│   └── src/main/java/com/cstracker/
│       ├── controller/         # REST endpoints (inventory, portfolio, market)
│       ├── entity/             # JPA entities (Item, Price, PortfolioSnapshot)
│       ├── model/              # Request/response records (SteamItem, InventoryItemView, ...)
│       ├── repository/         # Spring Data JPA repositories
│       ├── service/            # Business logic (price collection, Steam API, market data)
│       └── config/             # CORS and RestTemplate configuration
└── frontend/
    └── src/
        ├── pages/              # Dashboard, Inventory
        └── components/         # PortfolioChart, PortfolioValueChart, InventoryTable
```

## Limitations and future improvements

- **Storage Containers:** Steam's inventory API only exposes the base inventory (up to 1000 slots). Items stored inside Storage Containers are not visible to the API and cannot be tracked. Items must be moved to the base inventory to be included.
- **Trade cooldowns:** Newly traded items have a 7-day market cooldown during which they appear as non-marketable and are skipped by the price collection job.
- **Single inventory:** The app currently tracks only one hardcoded Steam inventory. A future improvement could allow multiple Steam IDs to be registered, each with their own nightly price collection and portfolio history, though this would require careful rate limit management across all tracked inventories.
- **Initial cost basis:** Items seen for the first time default to a zero cost basis, since the market price on the day they are discovered does not reflect their actual acquisition cost. The cost basis for pre-existing items must be backfilled manually.
- **Trade-out detection delay:** Because the app continues to price items that are not in a given Steam response (to absorb API gaps), truly traded-away items are only recognised after 7 consecutive days outside the inventory response.