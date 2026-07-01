package com.smartfuel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agent_id")
    private User agent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fuel_type_id", nullable = false)
    private FuelType fuelType;

    @Column(name = "quantity_liters", nullable = false)
    private Double quantityLiters;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Column(name = "delivery_address", nullable = false, length = 255)
    private String deliveryAddress;

    @Column(nullable = false, length = 30)
    private String status; // CREATED, ACCEPTED, AGENT_ASSIGNED, OUT_FOR_DELIVERY, DELIVERED, REJECTED

    @Builder.Default
    @Column(name = "is_emergency", nullable = false)
    private boolean isEmergency = false;

    @Builder.Default
    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus = "PENDING"; // PENDING, PAID, FAILED

    @Column(name = "payment_method", length = 20)
    private String paymentMethod; // UPI, CARD, COD

    @Column(length = 6)
    private String otp;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "CREATED";
        }
        if (paymentStatus == null) {
            paymentStatus = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
