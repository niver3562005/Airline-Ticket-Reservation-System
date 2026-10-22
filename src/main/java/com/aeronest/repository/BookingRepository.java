package com.aeronest.repository;

import com.aeronest.domain.Booking;
import com.aeronest.domain.DomainEnums.BookingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @EntityGraph(attributePaths = {"passengers", "flight", "flight.origin", "flight.destination", "flight.aircraft"})
    Optional<Booking> findByPnrIgnoreCase(String pnr);
    @EntityGraph(attributePaths = {"passengers", "flight", "flight.origin", "flight.destination"})
    Optional<Booking> findByPnrIgnoreCaseAndContactEmailIgnoreCase(String pnr, String email);
    @EntityGraph(attributePaths = {"passengers", "flight"})
    List<Booking> findAllByOrderByCreatedAtDesc();
    @EntityGraph(attributePaths = {"passengers", "flight", "flight.origin", "flight.destination"})
    List<Booking> findByContactEmailIgnoreCaseOrderByCreatedAtDesc(String email);
    @EntityGraph(attributePaths = {"passengers", "flight"})
    List<Booking> findByFlightIdAndStatusNot(Long flightId, BookingStatus status);
    long countByStatus(BookingStatus status);
    long countByCreatedAtAfter(LocalDateTime time);
}
