package com.smartfuel.repository;

import com.smartfuel.entity.DeliveryAgent;
import com.smartfuel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeliveryAgentRepository extends JpaRepository<DeliveryAgent, Long> {
    Optional<DeliveryAgent> findByUser(User user);
    List<DeliveryAgent> findByStatus(String status);
}
