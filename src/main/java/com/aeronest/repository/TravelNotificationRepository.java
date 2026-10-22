package com.aeronest.repository;

import com.aeronest.domain.TravelNotification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TravelNotificationRepository extends JpaRepository<TravelNotification, Long> {
    @EntityGraph(attributePaths = {"booking", "booking.flight"})
    List<TravelNotification> findTop50ByAccountIdOrderByCreatedAtDesc(Long accountId);
    @EntityGraph(attributePaths = {"booking", "booking.flight"})
    List<TravelNotification> findTop50ByAccountIdAndChannelOrderByCreatedAtDesc(Long accountId, com.aeronest.domain.DomainEnums.NotificationChannel channel);
    long countByAccountIdAndReadAtIsNullAndChannel(Long accountId, com.aeronest.domain.DomainEnums.NotificationChannel channel);
    List<TravelNotification> findTop50ByOrderByCreatedAtDesc();
}
