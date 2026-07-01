package com.smartfuel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Builder.Default
    @Column(nullable = false)
    private boolean verified = false;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
        if (expiryTime == null) {
            expiryTime = generatedAt.plusMinutes(15); // Default 15 minutes expiry
        }
    }
}
