package com.smartfuel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fuel_type_id", nullable = false)
    private FuelType fuelType;

    @Column(nullable = false)
    private Double price;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "updated_by_id", nullable = false)
    private User updatedBy;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }
}
