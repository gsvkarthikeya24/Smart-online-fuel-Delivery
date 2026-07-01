package com.smartfuel.repository;

import com.smartfuel.entity.Review;
import com.smartfuel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProvider(User provider);
    List<Review> findByCustomer(User customer);
}
