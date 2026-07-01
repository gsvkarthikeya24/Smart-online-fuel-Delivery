package com.smartfuel.repository;

import com.smartfuel.entity.Payment;
import com.smartfuel.entity.Order;
import com.smartfuel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);
    Optional<Payment> findByTransactionId(String transactionId);
    List<Payment> findByOrderCustomer(User customer);
}
