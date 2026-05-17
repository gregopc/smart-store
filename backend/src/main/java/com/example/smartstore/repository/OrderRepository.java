package com.example.smartstore.repository;

import com.example.smartstore.domain.Order;
import com.example.smartstore.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByIdAndUser(UUID id, User user);
    List<Order> findByUser(User user);
}
