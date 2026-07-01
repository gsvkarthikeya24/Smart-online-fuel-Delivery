package com.smartfuel.repository;

import com.smartfuel.entity.Order;
import com.smartfuel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomer(User customer, Pageable pageable);
    List<Order> findByCustomer(User customer);
    Page<Order> findByProvider(User provider, Pageable pageable);
    List<Order> findByProvider(User provider);
    Page<Order> findByAgent(User agent, Pageable pageable);
    List<Order> findByAgent(User agent);
    
    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:fuelTypeId IS NULL OR o.fuelType.id = :fuelTypeId) AND " +
           "(:customerUsername IS NULL OR o.customer.username LIKE %:customerUsername%) AND " +
           "(o.createdAt >= :startDate AND o.createdAt <= :endDate)")
    Page<Order> searchOrders(@Param("status") String status,
                             @Param("fuelTypeId") Long fuelTypeId,
                             @Param("customerUsername") String customerUsername,
                             @Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate,
                             Pageable pageable);
                             
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
