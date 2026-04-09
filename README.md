# CS Investment Tracker

A personal CS2 skin investment tracker that monitors portfolio value over time and compares it against the S&P 500.

## Stack

- **Backend:** Java 21 / Spring Boot 3, containerized with Docker for consistent, reproducible deployments, deployed on Railway
- **Database:** PostgreSQL, managed by Railway
- **Frontend:** React, planned, will be deployed publicly

## Architecture

tbd

## What is the CS2 skin market?

CS2 (Counter-Strike 2) is a free-to-play shooter with an in-game economy where players own cosmetic weapon skins. These skins are stored on your Steam account and can be freely traded, bought, and sold. Unlike most in-game items, CS2 skins have real monetary value.

**Converting skins to money:**
- **Steam balance:** Skins sold on the Steam Market convert to Steam wallet funds, which can be used to purchase games, hardware like the Steam Deck, or other items
- **Third-party platforms:** Sites like Skinport, CS.Money, and others allow users to sell skins for real money transferred directly to a bank account, PayPal, or crypto

**The market as an investment:**
The CS2 skin market has been active since 2013 and has grown into a multi-billion dollar economy. The overall market has historically shown steady appreciation over long time horizons, with some analyses placing its performance on par with or above the S&P 500 over 5-10 year periods. Unlike stocks, the market is driven by a combination of game popularity, item rarity, and cultural moments (major tournament stickers, limited releases), making certain items highly sought after collectibles.

That said, individual items can be volatile. The market behaves more like an index when looked at in aggregate, smoothing out the swings of individual skins. This tracker takes that view, measuring total portfolio value rather than betting on single items.

## Steam API limitations

This app currently only works for a single, hardcoded Steam inventory (mine). This is a deliberate design choice due to Steam API restrictions:

- The Steam community inventory endpoint is public but heavily rate limited
- There is no official API for real-time CS2 market prices
- Many third-party sites bypass these limits by running large networks of Steam bot accounts that scrape data continuously, which violates Steam's Terms of Service

This app takes the compliant approach: a nightly price collection job that respects rate limits (one request every 43 seconds). This works well in practice because CS2 skin prices don't fluctuate significantly day-to-day, making daily snapshots sufficient for investment tracking.

## How it works

A nightly job runs at 2 AM UTC, fetches the Steam inventory, and collects current Steam Market prices for each item. Prices are saved to the database daily, building a historical record over time. A portfolio snapshot (total value + item count) is also saved each night.


## Limitations and future improvements

- **Storage Containers:** Steam's inventory API only exposes the base inventory (up to 1000 slots). Items stored inside Storage Containers are not visible to the API and cannot be tracked. Items must be moved to the base inventory to be included.
- **Trade cooldowns:** Newly traded items have a 7-day market cooldown during which they appear as non-marketable and are skipped by the price collection job.
- **Single inventory:** The app currently tracks only one hardcoded Steam inventory. A future improvement could allow multiple Steam IDs to be registered, each with their own nightly price collection and portfolio history, though this would require careful rate limit management across all tracked inventories.

## Deployment

Backend and database are hosted on Railway. The frontend will be deployed separately as a public React app that consumes the Railway backend API.
