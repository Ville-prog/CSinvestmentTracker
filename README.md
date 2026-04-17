# CS Investment Tracker

A personal CS2 skin investment tracker that records the daily market value of a Steam inventory, charts its returns over time, and compares them against the S&P 500. Essentially a stock portfolio tracker, but for CS2 skins.

## Live Demo

[csinvestmenttracker.vercel.app](https://csinvestmenttracker.vercel.app/)

Read-only view of my personal inventory.

## What is the CS2 skin market?

CS2 (Counter-Strike 2) is a free-to-play competitive shooter made by Valve with an **in-game economy** where players own cosmetic weapon skins. These skins are stored on your Steam account and can be freely traded, bought, and sold. Unlike most in-game items, **CS2 skins have real monetary value.**

**Converting skins to money:**
- **Steam balance:** Skins sold on the Steam Market convert to Steam wallet funds, which can be used to purchase games, hardware like the Steam Deck, or other items
- **Third-party platforms:** Sites like Skinport, CS.Money, and others allow users to sell skins for real money transferred directly to a bank account, PayPal, or crypto

**The market as an investment:**
The CS2 skin market has been active since 2013 and has grown into a **multi-billion dollar economy.** Held in aggregate, the market has generally appreciated over long time horizons.

Individual items can still be volatile. **The market behaves more like an index** when looked at in aggregate, smoothing out the swings of individual skins. **This tracker takes that view, measuring total portfolio value rather than betting on single items.**

## Steam API limitations

This app currently only works for a single, hardcoded Steam inventory (mine). This is a deliberate design choice due to Steam API restrictions:

- The Steam community inventory endpoint is public but heavily rate limited
- There is no official API for real-time CS2 market prices
- Truncated or silently filtered responses. Even within rate limits, Steam will occasionally return a partial snapshot of an inventory with no error signal.

Many third-party tracker sites work around these by running networks of Steam bot accounts that scrape the endpoints continuously, which violates Steam's Terms of Service. This app takes the compliant approach instead: a single nightly job that respects rate limits (one request every 4 seconds) and is designed defensively around truncation and gaps.

## How it works

The nightly job runs at 11 PM UTC in two stages. First, it fetches the Steam inventory and upserts any newly discovered items into the database. Second, it collects the current Steam Market price for **every tracked item in the database**, not only the items returned by today's Steam response. Prices are saved daily, building a historical record over time, and a portfolio snapshot (total value, total cost basis, unit count) is saved at the end of each run.

Because pricing is driven by the DB rather than the Steam response, transient gaps don't distort the chart: tracked items still get priced even on days Steam returns fewer of them. Items missing from Steam for more than 7 consecutive days are considered traded away.

## P&L calculation

Portfolio profit/loss is tracked through two charts on the dashboard:

**1. P&L %:** CS2 portfolio profit/loss as a percentage. The S&P 500 line can be toggled on or off for comparison. Calculated as profit/loss relative to cost basis:

```
P&L % = (current value - cost basis) / cost basis * 100
```

When the tracked quantity of an existing item increases (new units acquired), those new units are added to the cost basis at today's market price. When it decreases (units sold), the cost basis is scaled proportionally. Items seen for the very first time default to a cost basis of zero, since today's price is not a valid proxy for the original acquisition cost of a pre-existing stack, so the initial cost basis must be set manually in the database. **This means the chart only moves when prices change; adding new items to the inventory does not affect the P&L line.** This mirrors how real investment portfolio trackers work: buying new shares increases your portfolio value, but does not count as a gain. Only price appreciation of what you already hold moves the return percentage.

**2. Total Value:** Raw portfolio value in EUR over time. Unlike the P&L chart, this reflects absolute value including the effect of adding new items to the inventory.

## Stack

- **Backend:** Java 21 / Spring Boot 3, deployed on Railway via Docker
- **Database:** PostgreSQL, managed through Railway
- **Frontend:** React with Recharts, deployed on Vercel
- **External APIs:** Steam Community Market (prices), Yahoo Finance (S&P 500 history)

## Structure

High-level flow: `Steam API → nightly job → PostgreSQL → Spring Boot REST → React on Vercel`.

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

## Running locally

Requires Java 21, Node 18+, and a local PostgreSQL instance.

```bash
# backend (from /backend)
export DB_PASSWORD=yourpassword
export ADMIN_SECRET=anything
./mvnw spring-boot:run

# frontend (from /frontend)
npm install
npm start
```

The backend reads `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`, `STEAM_USER_ID`, and `ADMIN_SECRET` from the environment. Defaults in [application.properties](backend/src/main/resources/application.properties) cover most local setups. The frontend reads `REACT_APP_API_URL` and falls back to `http://localhost:8080`.

## Limitations and future improvements

- **Storage Containers:** Steam's inventory API only exposes the base inventory (up to 1000 slots). Items stored inside Storage Containers are not visible to the API and cannot be tracked. Items must be moved to the base inventory to be included.
- **Trade cooldowns:** Newly traded items have a 7-day market cooldown during which they appear as non-marketable and are skipped by the price collection job.
- **Single inventory:** The deployed live app currently tracks only one hardcoded Steam inventory. A future improvement could allow multiple Steam IDs to be registered, each with their own nightly price collection and portfolio history, though this would require careful rate limit management across all tracked inventories.
- **Initial cost basis:** Items seen for the first time default to a zero cost basis, since the market price on the day they are discovered does not reflect their actual acquisition cost. The cost basis for pre-existing items must be backfilled manually.
- **Trade-out detection delay:** Because the app continues to price items that are not in a given Steam response (to absorb API gaps), truly traded-away items are only recognised after 7 consecutive days outside the inventory response.