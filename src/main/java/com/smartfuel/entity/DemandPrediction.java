package com.smartfuel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "demand_predictions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fuel_type_id", nullable = false)
    private FuelType fuelType;

    @Column(name = "prediction_date", nullable = false)
    private LocalDate predictionDate;

    @Column(name = "predicted_demand_liters", nullable = false)
    private Double predictedDemandLiters;

    @Column(name = "confidence_score", nullable = false)
    private Double confidenceScore;

    @Column(name = "model_name", length = 50)
    private String modelName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
