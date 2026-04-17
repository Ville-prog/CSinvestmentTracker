/**
 * PriceCollectionJobTest.java
 *
 * Unit tests for PriceCollectionJob covering cost-basis tracking, the last-seen grace window,
 * and the sanity gate that protects against degraded Steam responses.
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceCollectionJobTest {

    @Mock SteamApiService steamApiService;
    @Mock PriceService priceService;
    @Mock ItemRepository itemRepository;
    @Mock PriceRepository priceRepository;
    @Mock PortfolioSnapshotRepository snapshotRepository;

    @InjectMocks PriceCollectionJob job;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(job, "steamUserId", "12345");
        job.rateLimitDelayMs = 0;
        lenient().when(snapshotRepository.findByDate(any(LocalDate.class))).thenReturn(Optional.empty());
        lenient().when(priceRepository.findByItemAndDate(any(Item.class), any(LocalDate.class))).thenReturn(Optional.empty());
        lenient().when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private SteamItem steamItem(String name, int amount) {
        return new SteamItem("class-" + name, name, name, "icon", true, true, amount);
    }

    private Item existingItem(String name, double costBasisEur, int trackedQuantity, LocalDate lastSeen) {
        Item item = new Item();
        item.setId((long) name.hashCode());
        item.setMarketHashName(name);
        item.setName(name);
        item.setCostBasisEur(costBasisEur);
        item.setTrackedQuantity(trackedQuantity);
        item.setLastSeenInSteam(lastSeen);
        return item;
    }

    private Item lastSavedItem() {
        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository, atLeastOnce()).save(captor.capture());
        List<Item> all = captor.getAllValues();
        return all.get(all.size() - 1);
    }

    @Test
    void firstSeeDoesNotAddToCostBasis() {
        Item justCreated = existingItem("Foo", 0.0, 0, null);
        SteamItem fromSteam = steamItem("Foo", 5);
        when(steamApiService.getInventory("12345")).thenReturn(List.of(fromSteam));
        when(itemRepository.findByMarketHashName("Foo")).thenReturn(Optional.of(justCreated));
        when(itemRepository.findByTrackedQuantityGreaterThan(0)).thenReturn(List.of());
        when(priceService.fetchPrice("Foo")).thenReturn(10.0);

        job.collectPrices();

        assertEquals(0.0, justCreated.getCostBasisEur(), 0.001);
        assertEquals(5, justCreated.getTrackedQuantity());
    }

    @Test
    void quantityIncreaseAddsToCostBasis() {
        Item existing = existingItem("Foo", 20.0, 2, LocalDate.now().minusDays(1));
        SteamItem fromSteam = steamItem("Foo", 5);
        when(steamApiService.getInventory("12345")).thenReturn(List.of(fromSteam));
        when(itemRepository.findByMarketHashName("Foo")).thenReturn(Optional.of(existing));
        when(itemRepository.findByTrackedQuantityGreaterThan(0)).thenReturn(List.of(existing));
        when(priceService.fetchPrice("Foo")).thenReturn(10.0);

        job.collectPrices();

        assertEquals(50.0, existing.getCostBasisEur(), 0.001);
        assertEquals(5, existing.getTrackedQuantity());
    }

    @Test
    void quantityDecreaseScalesCostBasis() {
        Item existing = existingItem("Foo", 100.0, 5, LocalDate.now().minusDays(1));
        SteamItem fromSteam = steamItem("Foo", 2);
        when(steamApiService.getInventory("12345")).thenReturn(List.of(fromSteam));
        when(itemRepository.findByMarketHashName("Foo")).thenReturn(Optional.of(existing));
        when(itemRepository.findByTrackedQuantityGreaterThan(0)).thenReturn(List.of(existing));
        when(priceService.fetchPrice("Foo")).thenReturn(10.0);

        job.collectPrices();

        assertEquals(40.0, existing.getCostBasisEur(), 0.001);
        assertEquals(2, existing.getTrackedQuantity());
    }

    @Test
    void emptyInventoryDoesNotSaveSnapshot() {
        when(steamApiService.getInventory("12345")).thenReturn(List.of());

        job.collectPrices();

        verify(snapshotRepository, never()).save(any(PortfolioSnapshot.class));
        verify(priceRepository, never()).save(any(Price.class));
    }

    @Test
    void pricesTrackedItemsNotInResponse() {
        LocalDate today = LocalDate.now();
        Item inResponse = existingItem("Foo", 20.0, 2, today.minusDays(1));
        Item missingFromResponse = existingItem("Bar", 15.0, 1, today.minusDays(1));
        SteamItem fromSteam = steamItem("Foo", 2);
        when(steamApiService.getInventory("12345")).thenReturn(List.of(fromSteam));
        when(itemRepository.findByMarketHashName("Foo")).thenReturn(Optional.of(inResponse));
        lenient().when(itemRepository.findByMarketHashName("Bar")).thenReturn(Optional.of(missingFromResponse));
        when(itemRepository.findByTrackedQuantityGreaterThan(0)).thenReturn(new ArrayList<>(List.of(inResponse, missingFromResponse)));
        when(priceService.fetchPrice("Foo")).thenReturn(10.0);
        when(priceService.fetchPrice("Bar")).thenReturn(7.0);

        job.collectPrices();

        ArgumentCaptor<PortfolioSnapshot> captor = ArgumentCaptor.forClass(PortfolioSnapshot.class);
        verify(snapshotRepository).save(captor.capture());
        PortfolioSnapshot snap = captor.getValue();
        assertEquals(3, snap.getItemCount());
        assertEquals(27.0, snap.getTotalValueEur(), 0.001);
        verify(priceService).fetchPrice("Bar");
    }

    @Test
    void agesOutItemsUnseenPastGraceWindow() {
        LocalDate today = LocalDate.now();
        Item stale = existingItem("Old", 50.0, 2, today.minusDays(PriceCollectionJob.GRACE_DAYS + 1));
        Item fresh = existingItem("New", 30.0, 1, today.minusDays(1));
        List<Item> allItems = new ArrayList<>(List.of(stale, fresh));
        SteamItem freshFromSteam = steamItem("New", 1);
        when(steamApiService.getInventory("12345")).thenReturn(List.of(freshFromSteam));
        lenient().when(itemRepository.findByMarketHashName("Old")).thenReturn(Optional.of(stale));
        when(itemRepository.findByMarketHashName("New")).thenReturn(Optional.of(fresh));
        when(itemRepository.findByTrackedQuantityGreaterThan(0)).thenAnswer(inv ->
                allItems.stream().filter(i -> i.getTrackedQuantity() > 0).toList());
        when(priceService.fetchPrice("New")).thenReturn(10.0);

        job.collectPrices();

        assertEquals(0, stale.getTrackedQuantity());
        assertEquals(1, fresh.getTrackedQuantity());
        ArgumentCaptor<PortfolioSnapshot> snapCaptor = ArgumentCaptor.forClass(PortfolioSnapshot.class);
        verify(snapshotRepository).save(snapCaptor.capture());
        assertEquals(1, snapCaptor.getValue().getItemCount());
    }

    @Test
    void degradedRunDoesNotAgeOutOrUpdateTrackedQuantity() {
        LocalDate today = LocalDate.now();
        Item tracked = existingItem("Foo", 20.0, 10, today.minusDays(PriceCollectionJob.GRACE_DAYS + 1));
        SteamItem tinyResponse = steamItem("Foo", 10);
        when(steamApiService.getInventory("12345")).thenReturn(List.of(tinyResponse));
        when(snapshotRepository.findMaxItemCountSince(any(LocalDate.class))).thenReturn(1000);
        when(itemRepository.findByMarketHashName("Foo")).thenReturn(Optional.of(tracked));
        when(itemRepository.findByTrackedQuantityGreaterThan(0)).thenReturn(new ArrayList<>(List.of(tracked)));
        when(priceService.fetchPrice("Foo")).thenReturn(5.0);

        job.collectPrices();

        assertEquals(10, tracked.getTrackedQuantity());
        assertEquals(20.0, tracked.getCostBasisEur(), 0.001);
        assertTrue(tracked.getLastSeenInSteam().isBefore(today),
                "last_seen should NOT have been advanced during a degraded run");
    }

    @Test
    void saneRunUpdatesLastSeen() {
        LocalDate today = LocalDate.now();
        Item tracked = existingItem("Foo", 20.0, 5, today.minusDays(2));
        SteamItem fromSteam = steamItem("Foo", 5);
        when(steamApiService.getInventory("12345")).thenReturn(List.of(fromSteam));
        when(itemRepository.findByMarketHashName("Foo")).thenReturn(Optional.of(tracked));
        when(itemRepository.findByTrackedQuantityGreaterThan(0)).thenReturn(List.of(tracked));
        when(priceService.fetchPrice("Foo")).thenReturn(10.0);

        job.collectPrices();

        assertEquals(today, tracked.getLastSeenInSteam());
    }

    @Test
    void skipsItemsThatFailedToPrice() {
        LocalDate today = LocalDate.now();
        Item tracked = existingItem("Foo", 20.0, 2, today.minusDays(1));
        SteamItem fromSteam = steamItem("Foo", 2);
        when(steamApiService.getInventory("12345")).thenReturn(List.of(fromSteam));
        when(itemRepository.findByMarketHashName("Foo")).thenReturn(Optional.of(tracked));
        when(itemRepository.findByTrackedQuantityGreaterThan(0)).thenReturn(List.of(tracked));
        when(priceService.fetchPrice("Foo")).thenReturn(0.0);

        job.collectPrices();

        verify(priceRepository, never()).save(any(Price.class));
        ArgumentCaptor<PortfolioSnapshot> snapCaptor = ArgumentCaptor.forClass(PortfolioSnapshot.class);
        verify(snapshotRepository).save(snapCaptor.capture());
        assertEquals(0, snapCaptor.getValue().getItemCount());
        assertEquals(0.0, snapCaptor.getValue().getTotalValueEur(), 0.001);
    }

    @Test
    void reusesExistingPriceRecordForToday() {
        LocalDate today = LocalDate.now();
        Item tracked = existingItem("Foo", 20.0, 3, today.minusDays(1));
        Price existingPrice = new Price();
        existingPrice.setItem(tracked);
        existingPrice.setDate(today);
        existingPrice.setPriceEur(8.0);
        SteamItem fromSteam = steamItem("Foo", 3);
        when(steamApiService.getInventory("12345")).thenReturn(List.of(fromSteam));
        when(itemRepository.findByMarketHashName("Foo")).thenReturn(Optional.of(tracked));
        when(itemRepository.findByTrackedQuantityGreaterThan(0)).thenReturn(List.of(tracked));
        when(priceRepository.findByItemAndDate(eq(tracked), eq(today))).thenReturn(Optional.of(existingPrice));

        job.collectPrices();

        verify(priceService, never()).fetchPrice(any());
        verify(priceRepository, never()).save(any(Price.class));
        ArgumentCaptor<PortfolioSnapshot> snapCaptor = ArgumentCaptor.forClass(PortfolioSnapshot.class);
        verify(snapshotRepository).save(snapCaptor.capture());
        assertEquals(24.0, snapCaptor.getValue().getTotalValueEur(), 0.001);
    }

    @Test
    void saneByDefaultWhenNoHistoricalBaseline() {
        Item justCreated = existingItem("Foo", 0.0, 0, null);
        SteamItem fromSteam = steamItem("Foo", 5);
        when(steamApiService.getInventory("12345")).thenReturn(List.of(fromSteam));
        when(snapshotRepository.findMaxItemCountSince(any(LocalDate.class))).thenReturn(null);
        when(itemRepository.findByMarketHashName("Foo")).thenReturn(Optional.of(justCreated));
        when(itemRepository.findByTrackedQuantityGreaterThan(0)).thenReturn(List.of());
        when(priceService.fetchPrice("Foo")).thenReturn(10.0);

        job.collectPrices();

        assertFalse(justCreated.getLastSeenInSteam() == null, "last_seen should have been set on a sane run with no baseline");
    }
}
