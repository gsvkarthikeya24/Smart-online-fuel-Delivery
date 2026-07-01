package com.smartfuel.repository;

import com.smartfuel.entity.TrustScore;
import com.smartfuel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrustScoreRepository extends JpaRepository<TrustScore, Long> {
    List<TrustScore> findByCustomerOrderByUpdatedAtDesc(User customer);
}
