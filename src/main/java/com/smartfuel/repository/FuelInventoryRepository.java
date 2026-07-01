package com.smartfuel.repository;

import com.smartfuel.entity.FuelInventory;
import com.smartfuel.entity.FuelType;
import com.smartfuel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FuelInventoryRepository extends JpaRepository<FuelInventory, Long> {
    List<FuelInventory> findByProvider(User provider);
    Optional<FuelInventory> findByProviderAndFuelType(User provider, FuelType fuelType);
    List<FuelInventory> findByCurrentStockLitersLessThan(Double threshold);
}
