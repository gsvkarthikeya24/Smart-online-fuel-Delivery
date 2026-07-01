package com.smartfuel.service;

import com.smartfuel.entity.DemandPrediction;
import com.smartfuel.entity.FuelInventory;
import com.smartfuel.entity.FuelType;
import com.smartfuel.entity.Order;
import com.smartfuel.repository.DemandPredictionRepository;
import com.smartfuel.repository.FuelTypeRepository;
import com.smartfuel.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DemandPredictionService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private FuelTypeRepository fuelTypeRepository;

    @Autowired
    private DemandPredictionRepository predictionRepository;

    /**
     * Predict demand for a fuel type on a specific target date based on past 30 days of data.
     */
    @Transactional
    public DemandPrediction generatePrediction(Long fuelTypeId) {
        FuelType fuelType = fuelTypeRepository.findById(fuelTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Fuel type not found"));

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Order> orders = orderRepository.findByCreatedAtBetween(thirtyDaysAgo, LocalDateTime.now()).stream()
                .filter(o -> o.getFuelType().getId().equals(fuelTypeId) && 
                            (o.getStatus().equals("DELIVERED") || o.getStatus().equals("REVIEWED") || o.getStatus().equals("ACCEPTED") || o.getStatus().equals("AGENT_ASSIGNED") || o.getStatus().equals("OUT_FOR_DELIVERY")))
                .collect(Collectors.toList());

        // Group order quantity by date
        Map<LocalDate, Double> dailySum = new HashMap<>();
        for (Order o : orders) {
            LocalDate date = o.getCreatedAt().toLocalDate();
            dailySum.put(date, dailySum.getOrDefault(date, 0.0) + o.getQuantityLiters());
        }

        double predictedDemand = 0.0;
        double confidenceScore = 0.85; // Default baseline confidence
        String modelName = "Simple Moving Average";

        if (dailySum.size() < 3) {
            // Default baseline if insufficient historical data
            predictedDemand = 250.0 + (Math.random() * 100.0); // 250L baseline + random noise
            confidenceScore = 0.50; // Low confidence due to low data
            modelName = "Baseline Estimate (Insufficient Data)";
        } else {
            // Use Linear Regression to predict next day
            List<LocalDate> sortedDates = dailySum.keySet().stream().sorted().collect(Collectors.toList());
            int n = sortedDates.size();
            double sumX = 0;
            double sumY = 0;
            double sumXY = 0;
            double sumXX = 0;

            for (int i = 0; i < n; i++) {
                double x = i + 1;
                double y = dailySum.get(sortedDates.get(i));
                sumX += x;
                sumY += y;
                sumXY += x * y;
                sumXX += x * x;
            }

            double meanX = sumX / n;
            double meanY = sumY / n;

            // Slope (m) and intercept (c) for y = mx + c
            double num = sumXY - (n * meanX * meanY);
            double den = sumXX - (n * meanX * meanX);
            
            double slope = den != 0 ? num / den : 0.0;
            double intercept = meanY - (slope * meanX);

            // Predict for next index (n + 1)
            predictedDemand = (slope * (n + 1)) + intercept;
            if (predictedDemand < 50.0) {
                // Keep a minimum floor forecast
                predictedDemand = meanY > 0 ? meanY : 150.0;
            }
            
            // Adjust confidence score by history depth
            confidenceScore = Math.min(0.95, 0.60 + (n * 0.015));
            modelName = "Linear Regression (Least Squares)";
        }

        // Save prediction
        DemandPrediction prediction = DemandPrediction.builder()
                .fuelType(fuelType)
                .predictionDate(LocalDate.now().plusDays(1))
                .predictedDemandLiters(Math.round(predictedDemand * 100.0) / 100.0)
                .confidenceScore(Math.round(confidenceScore * 100.0) / 100.0)
                .modelName(modelName)
                .build();

        return predictionRepository.save(prediction);
    }

    @Transactional
    public List<DemandPrediction> predictAllActiveFuels() {
        List<FuelType> activeTypes = fuelTypeRepository.findByActiveTrue();
        List<DemandPrediction> predictions = new ArrayList<>();
        for (FuelType ft : activeTypes) {
            predictions.add(generatePrediction(ft.getId()));
        }
        return predictions;
    }

    public List<DemandPrediction> getLatestPredictions() {
        List<FuelType> activeTypes = fuelTypeRepository.findByActiveTrue();
        List<DemandPrediction> latest = new ArrayList<>();
        for (FuelType ft : activeTypes) {
            predictionRepository.findByFuelTypeOrderByPredictionDateDesc(ft).stream()
                    .findFirst()
                    .ifPresent(latest::add);
        }
        return latest;
    }

    /**
     * Checks if any provider's stock is predicted to fall below low stock threshold in the next 48h
     */
    public boolean checkLowStockForecasting(FuelInventory inventory) {
        // Find average daily usage
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Order> orders = orderRepository.findByCreatedAtBetween(sevenDaysAgo, LocalDateTime.now()).stream()
                .filter(o -> o.getProvider().getId().equals(inventory.getProvider().getId()) && 
                            o.getFuelType().getId().equals(inventory.getFuelType().getId()) &&
                            o.getStatus().equals("DELIVERED"))
                .collect(Collectors.toList());

        double totalLiters = orders.stream().mapToDouble(Order::getQuantityLiters).sum();
        double avgDailyConsumption = totalLiters / 7.0;

        if (avgDailyConsumption <= 0) {
            avgDailyConsumption = 100.0; // Fallback baseline consumption
        }

        // If current stock cannot last 2 days of avg consumption, flag as warning
        return inventory.getCurrentStockLiters() < (avgDailyConsumption * 2.0);
    }
}
