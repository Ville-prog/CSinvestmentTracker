/**
 * PriceCollectionJob.java
 *
 * Scheduled nightly job that collects Steam Market prices for all tracked items and records
 * a daily portfolio snapshot. Uses a last-seen timestamp on each item and a sanity gate on the
 * Steam response to absorb transient API gaps: prices are fetched for every tracked item in the
 * database, not only the items returned by today's Steam call, and items unseen for more than
 * the grace window are treated as traded away.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.service;

import com.cstracker.entity.Item;
import com.cstracker.entity.PortfolioSnapshot;
import com.cstracker.entity.Price;
import com.cstracker.model.SteamItem;
import com.cstracker.repository.ItemRepository;
import com.cstracker.repository.PortfolioSnapshotRepository;
import com.cstracker.repository.PriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class PriceCollectionJob {

    private static final Logger log = LoggerFactory.getLogger(PriceCollectionJob.class);

    /** Number of days an item can be missing from Steam responses before it is considered traded away. */
    static final int GRACE_DAYS = 7;

    /**
     * Minimum fraction of the recent max item_count that today's Steam response must reach
     * to be treated as sane. Below this, the run is degraded: prices are still fetched for
     * tracked items, but last_seen and trackedQuantity are not touched so a bad Steam day
     * cannot falsely age out the entire inventory.
     */
    static final double SANITY_THRESHOLD = 0.5;

    /** Lookback window for the sanity baseline. */
    static final int SANITY_LOOKBACK_DAYS = 7;

    int rateLimitDelayMs = 4000;

    private final SteamApiService steamApiService;
    private final PriceService priceService;
    private final ItemRepository itemRepository;
    private final PriceRepository priceRepository;
    private final PortfolioSnapshotRepository snapshotRepository;

    @Value("${steam.user.id}")
    private String steamUserId;

    /**
     * Constructs the job with all required service and repository dependencies.
     *
     * @param steamApiService    service used to fetch the Steam inventory
     * @param priceService       service used to fetch individual item prices from Steam Market
     * @param itemRepository     repository for upserting item records
     * @param priceRepository    repository for saving daily price records
     * @param snapshotRepository repository for saving daily portfolio value snapshots
     */
    public PriceCollectionJob(SteamApiService steamApiService, PriceService priceService,
                              ItemRepository itemRepository, PriceRepository priceRepository,
                              PortfolioSnapshotRepository snapshotRepository) {
        this.steamApiService = steamApiService;
        this.priceService = priceService;
        this.itemRepository = itemRepository;
        this.priceRepository = priceRepository;
        this.snapshotRepository = snapshotRepository;
    }

    /**
     * Scheduled entry point. Fetches the Steam inventory and delegates to runCollection().
     * Scheduled at 23:00 UTC daily and can also be invoked manually via the admin endpoint.
     */
    @Scheduled(cron = "0 0 23 * * *")
    public void collectPrices() {
        List<SteamItem> steamItems = steamApiService.getInventory(steamUserId);
        log.info("Fetched {} items from Steam inventory response", steamItems.size());

        if (steamItems.isEmpty()) {
            log.warn("Steam inventory returned empty, skipping snapshot save for {} to avoid poisoning the chart.", LocalDate.now());
            return;
        }

        runCollection(steamItems);
    }

    /**
     * Runs price collection using a caller-supplied inventory, bypassing the Steam API fetch.
     * Used by the admin endpoint when the nightly Steam call has failed and the user supplies
     * the inventory JSON fetched manually from a browser session.
     *
     * @param steamItems pre-parsed inventory items to use as the Steam response for today's run
     */
    public void collectFromManualInventory(List<SteamItem> steamItems) {
        log.info("Starting manual price collection from provided inventory ({} items)", steamItems.size());

        if (steamItems.isEmpty()) {
            log.warn("Provided inventory is empty, skipping.");
            return;
        }

        runCollection(steamItems);
    }

    /**
     * Core collection logic shared by both the scheduled run and the manual inventory endpoint.
     * Validates the response against a sanity baseline, upserts items, updates last-seen timestamps,
     * ages out stale items, fetches prices for all tracked items, and saves a portfolio snapshot.
     *
     * @param steamItems the inventory items to process
     */
    private void runCollection(List<SteamItem> steamItems) {
        LocalDate today = LocalDate.now();
        log.info("Starting price collection for {}", today);

        if (snapshotRepository.findByDate(today).isPresent()) {
            log.info("Price collection already ran today, skipping.");
            return;
        }

        int responseUnits = steamItems.stream()
                .filter(SteamItem::marketable)
                .mapToInt(SteamItem::amount)
                .sum();
        boolean sane = isResponseSane(responseUnits, today);
        if (!sane) {
            log.warn("Steam response ({} marketable units) is below the sanity threshold against the " +
                    "{}-day baseline. Treating run as degraded: prices will be fetched for all tracked " +
                    "items in the DB, but last_seen and trackedQuantity will NOT be updated.",
                    responseUnits, SANITY_LOOKBACK_DAYS);
        }

        Map<String, SteamItem> responseByHash = new HashMap<>();
        for (SteamItem s : steamItems) {
            if (!s.marketable()) continue;
            responseByHash.put(s.marketHashName(), s);
            itemRepository.findByMarketHashName(s.marketHashName())
                    .orElseGet(() -> createNewItem(s));
        }

        if (sane) {
            markItemsSeen(responseByHash.keySet(), today);
            ageOutStaleItems(today);
        }

        List<Item> candidates = gatherPricingCandidates(responseByHash, sane);
        log.info("Pricing {} candidate items ({} from Steam response, sane={}).",
                candidates.size(), responseByHash.size(), sane);

        CollectionTotals totals = priceCandidates(candidates, responseByHash, sane, today);

        if (totals.interrupted) {
            log.warn("Price collection was interrupted after {} prices, saving partial snapshot.", totals.pricesCollected);
        }

        PortfolioSnapshot snapshot = new PortfolioSnapshot();
        snapshot.setDate(today);
        snapshot.setTotalValueEur(totals.totalValue);
        snapshot.setTotalCostBasisEur(totals.totalCostBasis);
        snapshot.setItemCount(totals.totalUnits);
        snapshotRepository.save(snapshot);

        log.info("Price collection complete. {} new prices saved. Total value: {}EUR across {} units.",
                totals.pricesCollected, totals.totalValue, totals.totalUnits);
    }

    private boolean isResponseSane(int responseUnits, LocalDate today) {
        Integer maxRecent = snapshotRepository.findMaxItemCountSince(today.minusDays(SANITY_LOOKBACK_DAYS));
        if (maxRecent == null || maxRecent == 0) {
            return true;
        }
        return responseUnits >= (int) Math.floor(maxRecent * SANITY_THRESHOLD);
    }

    private Item createNewItem(SteamItem s) {
        Item item = new Item();
        item.setMarketHashName(s.marketHashName());
        item.setName(s.name());
        item.setClassId(s.classId());
        item.setIconUrl(s.iconUrl());
        return itemRepository.save(item);
    }

    private void markItemsSeen(Set<String> hashesInResponse, LocalDate today) {
        for (String hash : hashesInResponse) {
            itemRepository.findByMarketHashName(hash).ifPresent(item -> {
                item.setLastSeenInSteam(today);
                itemRepository.save(item);
            });
        }
    }

    private void ageOutStaleItems(LocalDate today) {
        LocalDate cutoff = today.minusDays(GRACE_DAYS);
        List<Item> aged = itemRepository.findByTrackedQuantityGreaterThan(0).stream()
                .filter(i -> i.getLastSeenInSteam() != null && i.getLastSeenInSteam().isBefore(cutoff))
                .toList();
        for (Item item : aged) {
            log.info("Ageing out '{}', last seen in Steam on {} (more than {} days ago).",
                    item.getMarketHashName(), item.getLastSeenInSteam(), GRACE_DAYS);
            item.setTrackedQuantity(0);
            itemRepository.save(item);
        }
    }

    private List<Item> gatherPricingCandidates(Map<String, SteamItem> responseByHash, boolean sane) {
        List<Item> candidates = new ArrayList<>(itemRepository.findByTrackedQuantityGreaterThan(0));
        if (!sane) {
            return candidates;
        }
        Set<Long> existingIds = new HashSet<>();
        for (Item item : candidates) existingIds.add(item.getId());
        for (String hash : responseByHash.keySet()) {
            itemRepository.findByMarketHashName(hash).ifPresent(item -> {
                if (!existingIds.contains(item.getId())) {
                    candidates.add(item);
                    existingIds.add(item.getId());
                }
            });
        }
        return candidates;
    }

    private CollectionTotals priceCandidates(List<Item> candidates, Map<String, SteamItem> responseByHash,
                                             boolean sane, LocalDate today) {
        CollectionTotals totals = new CollectionTotals();
        for (Item item : candidates) {
            Optional<Price> existingPrice = priceRepository.findByItemAndDate(item, today);
            double price;
            boolean newPrice = existingPrice.isEmpty();
            if (newPrice) {
                price = priceService.fetchPrice(item.getMarketHashName());
                if (price <= 0.0) {
                    log.warn("Skipping price save for '{}', received price={}", item.getMarketHashName(), price);
                    if (sleep()) {
                        totals.interrupted = true;
                        return totals;
                    }
                    continue;
                }
            } else {
                price = existingPrice.get().getPriceEur();
            }

            if (sane) {
                reconcileQuantity(item, responseByHash.get(item.getMarketHashName()), price);
            }

            if (item.getTrackedQuantity() == 0) {
                if (newPrice && sleep()) {
                    totals.interrupted = true;
                    return totals;
                }
                continue;
            }

            if (newPrice) {
                Price priceRecord = new Price();
                priceRecord.setItem(item);
                priceRecord.setDate(today);
                priceRecord.setPriceEur(price);
                priceRepository.save(priceRecord);
                totals.pricesCollected++;
                if (sleep()) {
                    totals.interrupted = true;
                    return totals;
                }
            }

            totals.totalUnits += item.getTrackedQuantity();
            totals.totalValue += price * item.getTrackedQuantity();
            totals.totalCostBasis += item.getCostBasisEur();
        }
        return totals;
    }

    private void reconcileQuantity(Item item, SteamItem fromSteam, double price) {
        if (fromSteam == null || fromSteam.amount() == item.getTrackedQuantity()) {
            return;
        }
        int prior = item.getTrackedQuantity();
        if (prior > 0 && fromSteam.amount() > prior) {
            int newUnits = fromSteam.amount() - prior;
            item.setCostBasisEur(item.getCostBasisEur() + newUnits * price);
        } else if (prior > 0 && fromSteam.amount() < prior) {
            double ratio = (double) fromSteam.amount() / prior;
            item.setCostBasisEur(item.getCostBasisEur() * ratio);
            log.info("Quantity decreased for '{}': {} -> {}, cost basis adjusted to {}",
                    item.getMarketHashName(), prior, fromSteam.amount(), item.getCostBasisEur());
        }
        if (prior == 0) {
            item.setCostBasisEur(fromSteam.amount() * price);
        }
        item.setTrackedQuantity(fromSteam.amount());
        itemRepository.save(item);
    }

    private boolean sleep() {
        try {
            Thread.sleep(rateLimitDelayMs);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Price collection interrupted");
            return true;
        }
    }

    private static final class CollectionTotals {
        double totalValue;
        double totalCostBasis;
        int totalUnits;
        int pricesCollected;
        boolean interrupted;
    }
}
