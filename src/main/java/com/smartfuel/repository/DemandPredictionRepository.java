package com.smartfuel.repository;

import com.smartfuel.entity.DemandPrediction;
import com.smartfuel.entity.FuelType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DemandPredictionRepository extends JpaRepository<DemandPrediction, Long> {
    List<DemandPrediction> findByFuelTypeOrderByPredictionDateDesc(FuelType fuelType);
}
