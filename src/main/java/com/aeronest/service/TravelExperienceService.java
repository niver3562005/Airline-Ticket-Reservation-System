package com.aeronest.service;

import com.aeronest.domain.AirportFacility;
import com.aeronest.domain.CountryTravelInfo;
import com.aeronest.domain.TravelPackage;
import com.aeronest.repository.AirportFacilityRepository;
import com.aeronest.repository.CountryTravelInfoRepository;
import com.aeronest.repository.TravelPackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TravelExperienceService {
    private final CountryTravelInfoRepository countries;
    private final AirportFacilityRepository facilities;
    private final TravelPackageRepository packages;

    public TravelExperienceService(CountryTravelInfoRepository countries, AirportFacilityRepository facilities,
                                   TravelPackageRepository packages) {
        this.countries = countries; this.facilities = facilities; this.packages = packages;
    }

    public List<CountryTravelInfo> countries() { return countries.findAllByOrderByCountryAsc(); }
    public CountryTravelInfo country(String code) { return countries.findByCountryCodeIgnoreCase(code)
            .orElseThrow(() -> new ResourceNotFoundException("Travel guidance is not yet available for that country")); }
    public List<AirportFacility> facilities() { return facilities.findAllByOrderByAirportIataCodeAscTypeAsc(); }
    public List<AirportFacility> facilities(String iata) { return facilities.findByAirportIataCodeIgnoreCaseOrderByTypeAscNameAsc(iata); }
    public List<TravelPackage> packages() { return packages.findByActiveTrueOrderByPricePerPersonAsc(); }
}
