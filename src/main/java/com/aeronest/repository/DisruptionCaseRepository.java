package com.aeronest.repository;

import com.aeronest.domain.DisruptionCase;
import com.aeronest.domain.DomainEnums.DisruptionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DisruptionCaseRepository extends JpaRepository<DisruptionCase, Long> {
    @EntityGraph(attributePaths = {"booking", "affectedFlight", "affectedFlight.origin", "affectedFlight.destination", "alternateFlight"})
    Optional<DisruptionCase> findByBookingId(Long bookingId);
    @EntityGraph(attributePaths = {"booking", "affectedFlight", "alternateFlight"})
    List<DisruptionCase> findAllByOrderByCreatedAtDesc();
    long countByStatus(DisruptionStatus status);
}
