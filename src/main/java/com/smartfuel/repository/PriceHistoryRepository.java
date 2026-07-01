package com.smartfuel.repository;

import com.smartfuel.entity.PriceHistory;
import com.smartfuel.entity.FuelType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findByFuelTypeOrderByUpdatedAtDesc(FuelType fuelType);
}
