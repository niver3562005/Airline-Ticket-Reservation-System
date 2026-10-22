package com.aeronest.repository;

import com.aeronest.domain.AirportFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AirportFacilityRepository extends JpaRepository<AirportFacility, Long> {
    List<AirportFacility> findByAirportIataCodeIgnoreCaseOrderByTypeAscNameAsc(String iataCode);
    List<AirportFacility> findAllByOrderByAirportIataCodeAscTypeAsc();
}
