package com.smartfuel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
    private String transactionId;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod; // UPI, CARD, COD

    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus; // PENDING, PAID, FAILED

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @PrePersist
    protected void onCreate() {
        paymentDate = LocalDateTime.now();
    }
}
