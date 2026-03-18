package com.tracksuit.backend.service;

import com.tracksuit.backend.dto.OrderDTO;
import com.tracksuit.backend.model.Order;
import com.tracksuit.backend.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Order saveOrder(OrderDTO dto) {
        // Deduplication check
        if (orderRepository.existsByPlatformAndProductNameAndExpectedDate(
                dto.getPlatform(), dto.getProductName(), dto.getExpectedDate())) {
            log.info("Duplicate order skipped: {} - {} ({})",
                    dto.getPlatform(), dto.getProductName(), dto.getExpectedDate());
            return null;
        }

        Order order = new Order();
        order.setPlatform(dto.getPlatform());
        order.setProductName(dto.getProductName());
        order.setStatus(dto.getStatus());
        order.setExpectedDate(dto.getExpectedDate());

        Order saved = orderRepository.save(order);
        log.info("Order saved: {} - {} [{}]", saved.getPlatform(), saved.getProductName(), saved.getId());
        return saved;
    }
}
