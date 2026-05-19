package com.example.smartstore.repository;

import com.example.smartstore.domain.UserAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserActionRepository extends JpaRepository<UserAction, UUID> {
    long countByUserIdAndEventTypeAndProductId(UUID userId, String eventType, UUID productId);

    @Query("""
            select count(ua)
            from UserAction ua
            where ua.userId = :userId
              and ua.eventType = :eventType
              and lower(coalesce(ua.searchQuery, '')) like lower(concat('%', :term, '%'))
            """)
    long countSearchesContaining(
            @Param("userId") UUID userId,
            @Param("eventType") String eventType,
            @Param("term") String term);
}
