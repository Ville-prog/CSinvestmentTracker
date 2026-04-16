/**
 * PriceCollectionJob.java
 *
 * Scheduled nightly job that collects Steam Market prices for all items in the tracked inventory.
 * Upserts items into the database, saves daily price records, and creates a portfolio value snapshot.
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
import java.util.List;

@Component
public class PriceCollectionJob {

    private static final Logger log = LoggerFactory.getLogger(PriceCollectionJob.class);
    private static final int RATE_LIMIT_DELAY_MS = 129000;

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
     * @param steamApiService   service used to fetch the Steam inventory
     * @param priceService      service used to fetch individual item prices from Steam Market
     * @param itemRepository    repository for upserting item records
     * @param priceRepository   repository for saving daily price records
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
     * Runs the full price collection cycle. Fetches the Steam inventory, upserts items into the
     * database, collects a price per marketable item with rate limiting, and saves a daily portfolio
     * snapshot. Skips execution if a snapshot already exists for today to prevent double runs.
     * Snapshot is not saved if the job is interrupted mid-run to avoid partial data.
     * Scheduled to run at 23:00 UTC daily and can also be triggered manually via the API.
     */
    @Scheduled(cron = "0 0 23 * * *")
    public void collectPrices() {
        LocalDate today = LocalDate.now();
        log.info("Starting nightly price collection for {}", today);

        if (snapshotRepository.findByDate(today).isPresent()) {
            log.info("Price collection already ran today, skipping.");
            return;
        }

        List<SteamItem> items = steamApiService.getInventory(steamUserId);
        log.info("Fetched {} items from inventory", items.size());

        double totalValue = 0.0;
        double totalCostBasis = 0.0;
        int totalUnits = 0;
        int pricesCollected = 0;
        boolean interrupted = false;

        for (SteamItem steamItem : items) {
            if (!steamItem.marketable()) continue;

            Item item = itemRepository.findByMarketHashName(steamItem.marketHashName())
                    .orElseGet(() -> {
                        Item newItem = new Item();
                        newItem.setMarketHashName(steamItem.marketHashName());
                        newItem.setName(steamItem.name());
                        newItem.setClassId(steamItem.classId());
                        newItem.setIconUrl(steamItem.iconUrl());
                        return itemRepository.save(newItem);
                    });

            if (priceRepository.findByItemAndDate(item, today).isPresent()) {
                totalUnits += steamItem.amount();
                totalCostBasis += item.getCostBasisEur();
                totalValue += priceRepository.findByItemAndDate(item, today).get().getPriceEur() * steamItem.amount();
                if (steamItem.amount() != item.getTrackedQuantity()) {
                    item.setTrackedQuantity(steamItem.amount());
                    itemRepository.save(item);
                }
                continue;
            }

            double price = priceService.fetchPrice(steamItem.marketHashName());

            if (price <= 0.0) {
                log.warn("Skipping price save for '{}' — received price={}", steamItem.marketHashName(), price);
                try {
                    Thread.sleep(RATE_LIMIT_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Price collection interrupted");
                    interrupted = true;
                    break;
                }
                continue;
            }

            if (steamItem.amount() != item.getTrackedQuantity()) {
                if (steamItem.amount() > item.getTrackedQuantity()) {
                    int newUnits = steamItem.amount() - item.getTrackedQuantity();
                    item.setCostBasisEur(item.getCostBasisEur() + newUnits * price);
                } else {
                    double ratio = (double) steamItem.amount() / item.getTrackedQuantity();
                    item.setCostBasisEur(item.getCostBasisEur() * ratio);
                    log.info("Quantity decreased for '{}': {} -> {}, cost basis adjusted to {}",
                            steamItem.marketHashName(), item.getTrackedQuantity(), steamItem.amount(), item.getCostBasisEur());
                }
                item.setTrackedQuantity(steamItem.amount());
                itemRepository.save(item);
            }

            totalUnits += steamItem.amount();
            totalValue += price * steamItem.amount();
            totalCostBasis += item.getCostBasisEur();

            Price priceRecord = new Price();
            priceRecord.setItem(item);
            priceRecord.setDate(today);
            priceRecord.setPriceEur(price);
            priceRepository.save(priceRecord);
            pricesCollected++;

            try {
                Thread.sleep(RATE_LIMIT_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Price collection interrupted");
                interrupted = true;
                break;
            }
        }

        if (interrupted) {
            log.warn("Price collection was interrupted after {} prices — saving partial snapshot.", pricesCollected);
        }

        PortfolioSnapshot snapshot = new PortfolioSnapshot();
        snapshot.setDate(today);
        snapshot.setTotalValueEur(totalValue);
        snapshot.setTotalCostBasisEur(totalCostBasis);
        snapshot.setItemCount(totalUnits);
        snapshotRepository.save(snapshot);

        log.info("Price collection complete. {} prices saved. Total value: {}EUR", pricesCollected, totalValue);
    }
}
