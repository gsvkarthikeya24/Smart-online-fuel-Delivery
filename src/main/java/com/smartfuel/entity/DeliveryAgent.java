package com.smartfuel.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "delivery_agents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "vehicle_number", length = 30)
    private String vehicleNumber;

    @Column(name = "vehicle_type", length = 30)
    private String vehicleType;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String status = "AVAILABLE"; // AVAILABLE, BUSY, OFFLINE

    @Builder.Default
    @Column(name = "current_latitude")
    private Double currentLatitude = 0.0;

    @Builder.Default
    @Column(name = "current_longitude")
    private Double currentLongitude = 0.0;
}
