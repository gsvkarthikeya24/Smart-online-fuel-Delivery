package com.smartfuel.repository;

import com.smartfuel.entity.OtpVerification;
import com.smartfuel.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByOrder(Order order);
}
