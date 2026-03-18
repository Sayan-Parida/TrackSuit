package com.tracksuit.backend.repository;

import com.tracksuit.backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByPlatformAndProductNameAndExpectedDate(String platform, String productName, LocalDate expectedDate);
    List<Order> findAllByOrderByCreatedAtDesc();
}
