package com.example.smartstore.repository;

import com.example.smartstore.domain.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    List<Promotion> findByActiveTrue();
}
