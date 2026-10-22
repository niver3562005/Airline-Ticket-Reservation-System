package com.aeronest.repository;

import com.aeronest.domain.RefundRequest;
import com.aeronest.domain.DomainEnums.RefundStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
    @EntityGraph(attributePaths = {"booking", "booking.flight", "payment"})
    List<RefundRequest> findAllByOrderByCreatedAtDesc();
    Optional<RefundRequest> findByBookingId(Long bookingId);
    long countByStatusIn(List<RefundStatus> statuses);
}
