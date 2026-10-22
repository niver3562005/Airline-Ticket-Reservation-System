package com.aeronest.repository;

import com.aeronest.domain.Flight;
import com.aeronest.domain.DomainEnums.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findAllByOrderByDepartureAtAsc();
    List<Flight> findTop8ByDepartureAtAfterOrderByDepartureAtAsc(LocalDateTime now);
    long countByDepartureAtBetween(LocalDateTime start, LocalDateTime end);
    Optional<Flight> findFirstByFlightNumberIgnoreCaseAndDepartureAtBetween(String flightNumber, LocalDateTime start, LocalDateTime end);
    Optional<Flight> findFirstByFlightNumberIgnoreCase(String flightNumber);
    boolean existsByFlightNumberIgnoreCase(String flightNumber);

    @Query("select f from Flight f where f.origin.id = :origin and f.destination.id = :destination " +
           "and f.departureAt >= :start and f.departureAt < :end and f.status in :statuses order by f.departureAt")
    List<Flight> search(@Param("origin") Long origin, @Param("destination") Long destination,
                        @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                        @Param("statuses") List<FlightStatus> statuses);
}
