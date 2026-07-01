package com.smartfuel.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fuel_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "base_price_per_liter", nullable = false)
    private Double basePricePerLiter;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
