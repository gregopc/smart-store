package com.example.smartstore.repository;

import com.example.smartstore.domain.Cart;
import com.example.smartstore.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUser(User user);
}