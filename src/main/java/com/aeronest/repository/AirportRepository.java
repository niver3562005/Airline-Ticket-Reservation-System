package com.aeronest.repository;

import com.aeronest.domain.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AirportRepository extends JpaRepository<Airport, Long> {
    List<Airport> findAllByOrderByCityAsc();
    List<Airport> findByActiveTrueOrderByCityAsc();
    Optional<Airport> findByIataCodeIgnoreCase(String code);
}
