package com.aeronest.repository;

import com.aeronest.domain.CountryTravelInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CountryTravelInfoRepository extends JpaRepository<CountryTravelInfo, Long> {
    List<CountryTravelInfo> findAllByOrderByCountryAsc();
    Optional<CountryTravelInfo> findByCountryCodeIgnoreCase(String countryCode);
}
