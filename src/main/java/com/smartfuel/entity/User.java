package com.smartfuel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(length = 255)
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Trust Score fields for Customer role
    @Builder.Default
    @Column(name = "trust_score")
    private Integer trustScore = 100;

    @Builder.Default
    @Column(name = "trust_level")
    private String trustLevel = "BRONZE";

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (trustScore == null) {
            trustScore = 100;
        }
        if (trustLevel == null) {
            trustLevel = "BRONZE";
        }
    }
}