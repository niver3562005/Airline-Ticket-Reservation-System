package com.aeronest.repository;

import com.aeronest.domain.FlightTelemetry;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FlightTelemetryRepository extends JpaRepository<FlightTelemetry, Long> {
    @EntityGraph(attributePaths = {"flight", "flight.origin", "flight.destination"})
    Optional<FlightTelemetry> findByFlightId(Long flightId);
    @EntityGraph(attributePaths = {"flight", "flight.origin", "flight.destination"})
    List<FlightTelemetry> findAllByOrderByObservedAtDesc();
}
