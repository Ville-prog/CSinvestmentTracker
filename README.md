# CS Investment Tracker

A personal CS2 skin investment tracker that monitors portfolio value over time and compares it against the S&P 500.

## What is the CS2 skin market?

CS2 (Counter-Strike 2) is a free-to-play competetive shooter made by Valve with an **in-game economy** where players own cosmetic weapon skins. These skins are stored on your Steam account and can be freely traded, bought, and sold. Unlike most in-game items, **CS2 skins have real monetary value.**

**Converting skins to money:**
- **Steam balance:** Skins sold on the Steam Market convert to Steam wallet funds, which can be used to purchase games, hardware like the Steam Deck, or other items
- **Third-party platforms:** Sites like Skinport, CS.Money, and others allow users to sell skins for real money transferred directly to a bank account, PayPal, or crypto

**The market as an investment:**
The CS2 skin market has been active since 2013 and has grown into a **multi-billion dollar economy.** The overall market has historically shown steady appreciation over long time horizons, **with some analyses placing its performance on par with or above the S&P 500** over 5-10 year periods.

That said, individual items can be volatile. **The market behaves more like an index** when looked at in aggregate, smoothing out the swings of individual skins. **This tracker takes that view, measuring total portfolio value rather than betting on single items.**



## How it works

A nightly job runs at 11 PM UTC, fetches the Steam inventory, and collects current Steam Market prices for each item. Prices are saved to the database daily, building a historical record over time. A portfolio snapshot (total value + item count) is also saved each night.

## Charts

The dashboard has two chart views, selectable via tabs:

### P&L %

Shows CS2 portfolio profit/loss as a percentage compared against the S&P 500. The S&P 500 line can be toggled on or off. CS2 Portfolio is calculated as profit/loss relative to cost basis:

```
P&L % = (current value - cost basis) / cost basis * 100
```

Cost basis is recorded at the market price on the day each item first becomes marketable, either on the first inventory scan or when a trade cooldown expires. When more units of the same item are added later, they are recorded at that day's price, incrementally building the cost basis. **This means the portfolio line only moves when prices change, not when new items are added to the inventory.**

### Total Value

Shows raw portfolio value in EUR over time. Unlike the P&L chart, this reflects absolute value including the effect of adding new items to the inventory.

## Steam API limitations

This app currently only works for a single, hardcoded Steam inventory (mine). This is a deliberate design choice due to Steam API restrictions:

- The Steam community inventory endpoint is public but heavily rate limited
- There is no official API for real-time CS2 market prices
- Many third-party sites bypass these limits by running large networks of Steam bot accounts that scrape data continuously, which violates Steam's Terms of Service

This app takes the compliant approach: a nightly price collection job that respects rate limits (one request every 43 seconds). This works well in practice because CS2 skin prices don't fluctuate significantly day-to-day, making daily snapshots sufficient for investment tracking.

## Stack

- **Backend:** Java 21 / Spring Boot 3, containerized with Docker for consistent, reproducible deployments, deployed on Railway
- **Database:** PostgreSQL, managed by Railway
- **Frontend:** React, planned, will be deployed publicly

## Structure

tbd

## Limitations and future improvements

- **Storage Containers:** Steam's inventory API only exposes the base inventory (up to 1000 slots). Items stored inside Storage Containers are not visible to the API and cannot be tracked. Items must be moved to the base inventory to be included.
- **Trade cooldowns:** Newly traded items have a 7-day market cooldown during which they appear as non-marketable and are skipped by the price collection job.
- **Single inventory:** The app currently tracks only one hardcoded Steam inventory. A future improvement could allow multiple Steam IDs to be registered, each with their own nightly price collection and portfolio history, though this would require careful rate limit management across all tracked inventories.

## Deployment

Backend and database are hosted on Railway. The frontend will be deployed separately as a public React app that consumes the Railway backend API.
