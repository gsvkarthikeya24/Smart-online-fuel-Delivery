package com.smartfuel.repository;

import com.smartfuel.entity.FuelType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FuelTypeRepository extends JpaRepository<FuelType, Long> {
    List<FuelType> findByActiveTrue();
}
