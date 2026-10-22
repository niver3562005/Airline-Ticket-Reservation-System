package com.aeronest.repository;

import com.aeronest.domain.JourneyServiceRequest;
import com.aeronest.domain.DomainEnums.JourneyServiceStatus;
import com.aeronest.domain.DomainEnums.JourneyServiceType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface JourneyServiceRequestRepository extends JpaRepository<JourneyServiceRequest, Long> {
    @EntityGraph(attributePaths = {"booking", "booking.flight", "passenger"})
    List<JourneyServiceRequest> findAllByOrderByCreatedAtDesc();
    @EntityGraph(attributePaths = {"booking", "booking.flight", "passenger"})
    List<JourneyServiceRequest> findByBookingIdOrderByCreatedAtDesc(Long bookingId);
    boolean existsByBookingIdAndPassengerIdAndTypeAndStatusIn(Long bookingId, Long passengerId,
                                                               JourneyServiceType type,
                                                               Collection<JourneyServiceStatus> statuses);
}
