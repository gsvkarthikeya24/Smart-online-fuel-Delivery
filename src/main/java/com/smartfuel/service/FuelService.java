package com.smartfuel.service;

import com.smartfuel.entity.*;
import com.smartfuel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FuelService {

    @Autowired
    private FuelTypeRepository fuelTypeRepository;

    @Autowired
    private FuelInventoryRepository inventoryRepository;

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    public List<FuelType> getAllActiveFuelTypes() {
        return fuelTypeRepository.findByActiveTrue();
    }

    public List<FuelType> getAllFuelTypes() {
        return fuelTypeRepository.findAll();
    }

    public FuelType saveFuelType(FuelType fuelType) {
        return fuelTypeRepository.save(fuelType);
    }

    public Optional<FuelType> getFuelTypeById(Long id) {
        return fuelTypeRepository.findById(id);
    }

    public List<FuelInventory> getInventoryForProvider(User provider) {
        return inventoryRepository.findByProvider(provider);
    }

    @Transactional
    public FuelInventory updateInventoryStock(User provider, Long fuelTypeId, Double newStock, Double maxCapacity) {
        FuelType fuelType = fuelTypeRepository.findById(fuelTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid fuel type ID"));

        FuelInventory inventory = inventoryRepository.findByProviderAndFuelType(provider, fuelType)
                .orElse(FuelInventory.builder()
                        .provider(provider)
                        .fuelType(fuelType)
                        .currentStockLiters(0.0)
                        .maxCapacityLiters(maxCapacity != null ? maxCapacity : 5000.0)
                        .build());

        if (maxCapacity != null) {
            inventory.setMaxCapacityLiters(maxCapacity);
        }
        inventory.setCurrentStockLiters(newStock);
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public void updateFuelPrice(Long fuelTypeId, Double newPrice, User provider) {
        FuelType fuelType = fuelTypeRepository.findById(fuelTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid fuel type"));

        fuelType.setBasePricePerLiter(newPrice);
        fuelTypeRepository.save(fuelType);

        PriceHistory priceHistory = PriceHistory.builder()
                .fuelType(fuelType)
                .price(newPrice)
                .updatedBy(provider)
                .build();
        priceHistoryRepository.save(priceHistory);
    }

    public List<FuelInventory> getLowStockAlerts() {
        return inventoryRepository.findAll().stream()
                .filter(inv -> inv.getCurrentStockLiters() < (inv.getMaxCapacityLiters() * 0.15))
                .collect(Collectors.toList());
    }

    public List<FuelInventory> getLowStockAlertsForProvider(User provider) {
        return inventoryRepository.findByProvider(provider).stream()
                .filter(inv -> inv.getCurrentStockLiters() < (inv.getMaxCapacityLiters() * 0.15))
                .collect(Collectors.toList());
    }

    public List<PriceHistory> getPriceHistory(FuelType fuelType) {
        return priceHistoryRepository.findByFuelTypeOrderByUpdatedAtDesc(fuelType);
    }
}
