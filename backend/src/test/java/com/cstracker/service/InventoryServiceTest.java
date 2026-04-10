/**
 * InventoryServiceTest.java
 *
 * Unit tests for InventoryService P&L calculation, filtering, and sorting logic.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.service;

import com.cstracker.entity.Item;
import com.cstracker.entity.Price;
import com.cstracker.model.InventoryItemView;
import com.cstracker.repository.ItemRepository;
import com.cstracker.repository.PriceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    ItemRepository itemRepository;

    @Mock
    PriceRepository priceRepository;

    @InjectMocks
    InventoryService inventoryService;

    private Item makeItem(String name, double costBasis, int trackedQty) {
        Item item = new Item();
        item.setMarketHashName(name);
        item.setName(name);
        item.setCostBasisEur(costBasis);
        item.setTrackedQuantity(trackedQty);
        return item;
    }

    private Price makePrice(Item item, double priceEur) {
        Price price = new Price();
        price.setItem(item);
        price.setDate(LocalDate.now());
        price.setPriceEur(priceEur);
        return price;
    }

    @Test
    void calculatesPositivePnl() {
        Item item = makeItem("AK-47 | Redline (Field-Tested)", 10.0, 1);
        when(itemRepository.findAll()).thenReturn(List.of(item));
        when(priceRepository.findTopByItemOrderByDateDesc(item)).thenReturn(Optional.of(makePrice(item, 12.0)));

        List<InventoryItemView> result = inventoryService.getInventoryItems();

        assertEquals(1, result.size());
        assertEquals(20.0, result.get(0).pnlPct(), 0.01);
    }

    @Test
    void calculatesNegativePnl() {
        Item item = makeItem("Some Skin", 10.0, 1);
        when(itemRepository.findAll()).thenReturn(List.of(item));
        when(priceRepository.findTopByItemOrderByDateDesc(item)).thenReturn(Optional.of(makePrice(item, 8.0)));

        List<InventoryItemView> result = inventoryService.getInventoryItems();

        assertEquals(1, result.size());
        assertEquals(-20.0, result.get(0).pnlPct(), 0.01);
    }

    @Test
    void returnsZeroPnlWhenCostBasisIsZero() {
        Item item = makeItem("New Item", 0.0, 1);
        when(itemRepository.findAll()).thenReturn(List.of(item));
        when(priceRepository.findTopByItemOrderByDateDesc(item)).thenReturn(Optional.of(makePrice(item, 5.0)));

        List<InventoryItemView> result = inventoryService.getInventoryItems();

        assertEquals(0.0, result.get(0).pnlPct(), 0.001);
    }

    @Test
    void excludesItemsWithZeroTrackedQuantity() {
        Item item = makeItem("Non-marketable Skin", 0.0, 0);
        when(itemRepository.findAll()).thenReturn(List.of(item));

        List<InventoryItemView> result = inventoryService.getInventoryItems();

        assertTrue(result.isEmpty());
    }

    @Test
    void excludesItemsWithNoPriceRecord() {
        Item item = makeItem("Unpriced Skin", 10.0, 1);
        when(itemRepository.findAll()).thenReturn(List.of(item));
        when(priceRepository.findTopByItemOrderByDateDesc(item)).thenReturn(Optional.empty());

        List<InventoryItemView> result = inventoryService.getInventoryItems();

        assertTrue(result.isEmpty());
    }

    @Test
    void sortsByTotalValueDescending() {
        Item cheap = makeItem("Cheap Skin", 1.0, 1);
        Item expensive = makeItem("Expensive Skin", 50.0, 1);
        when(itemRepository.findAll()).thenReturn(List.of(cheap, expensive));
        when(priceRepository.findTopByItemOrderByDateDesc(cheap)).thenReturn(Optional.of(makePrice(cheap, 2.0)));
        when(priceRepository.findTopByItemOrderByDateDesc(expensive)).thenReturn(Optional.of(makePrice(expensive, 100.0)));

        List<InventoryItemView> result = inventoryService.getInventoryItems();

        assertEquals("Expensive Skin", result.get(0).name());
        assertEquals("Cheap Skin", result.get(1).name());
    }

    @Test
    void calculatesCorrectTotalValueForMultipleUnits() {
        Item item = makeItem("AWP | Asiimov (Field-Tested)", 30.0, 3);
        when(itemRepository.findAll()).thenReturn(List.of(item));
        when(priceRepository.findTopByItemOrderByDateDesc(item)).thenReturn(Optional.of(makePrice(item, 15.0)));

        List<InventoryItemView> result = inventoryService.getInventoryItems();

        assertEquals(45.0, result.get(0).totalValueEur(), 0.001);
    }
}
