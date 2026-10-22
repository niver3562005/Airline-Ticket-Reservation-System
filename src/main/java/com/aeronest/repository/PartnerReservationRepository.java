package com.aeronest.repository;

import com.aeronest.domain.PartnerReservation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PartnerReservationRepository extends JpaRepository<PartnerReservation, Long> {
    @EntityGraph(attributePaths = {"partner", "partner.airport"})
    List<PartnerReservation> findAllByOrderByCreatedAtDesc();
    @EntityGraph(attributePaths = {"partner", "partner.airport"})
    List<PartnerReservation> findByAccountIdOrderByCreatedAtDesc(Long accountId);
    @EntityGraph(attributePaths = {"partner", "partner.airport"})
    Optional<PartnerReservation> findByReferenceIgnoreCase(String reference);
}
