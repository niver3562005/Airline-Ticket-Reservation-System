package com.aeronest.service;

import com.aeronest.domain.Airport;
import com.aeronest.domain.Flight;
import com.aeronest.domain.DomainEnums.FlightStatus;
import com.aeronest.repository.AirportRepository;
import com.aeronest.repository.FlightRepository;
import com.aeronest.repository.CountryTravelInfoRepository;
import com.aeronest.repository.TravelPartnerRepository;
import com.aeronest.domain.DomainEnums.PartnerType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AdvancedTravelServiceIntegrationTest {
    @Autowired FlightService service;
    @Autowired FlightRepository flights;
    @Autowired AirportRepository airports;
    @Autowired CountryTravelInfoRepository countries;
    @Autowired TravelPartnerRepository partners;

    @Test void findsAValidatedOneStopConnection() {
        Flight first = flights.findFirstByFlightNumberIgnoreCase("AN310").orElseThrow();
        Airport bangkok = airports.findByIataCodeIgnoreCase("BKK").orElseThrow();

        assertThat(service.searchConnections(first.getOrigin().getId(), bangkok.getId(), first.getDepartureAt().toLocalDate()))
                .anySatisfy(option -> {
                    assertThat(option.first().getFlightNumber()).isEqualTo("AN310");
                    assertThat(option.second().getFlightNumber()).isEqualTo("AN845");
                    assertThat(option.layoverMinutes()).isBetween(45L, 360L);
                });
    }

    @Test void rejectsAircraftScheduleOverlapAndInsufficientTurnaround() {
        Flight existing = flights.findFirstByFlightNumberIgnoreCase("AN204").orElseThrow();
        Flight conflict = new Flight();
        conflict.setFlightNumber("AN999");
        conflict.setDepartureAt(existing.getDepartureAt().plusMinutes(15));
        conflict.setArrivalAt(existing.getArrivalAt().plusMinutes(15));
        conflict.setDepartureGate("T1");
        conflict.setEconomyFare(new BigDecimal("10000"));
        conflict.setBusinessFare(new BigDecimal("20000"));
        conflict.setBaggageAllowanceKg(23);
        conflict.setStatus(FlightStatus.SCHEDULED);

        assertThatThrownBy(() -> service.save(conflict, existing.getOrigin().getId(), existing.getDestination().getId(), existing.getAircraft().getId()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("overlapping flight");
    }

    @Test void seedsFiftyDestinationsAndImageBackedHotels() {
        assertThat(countries.count()).isGreaterThanOrEqualTo(50);
        assertThat(countries.findAllByOrderByCountryAsc()).allSatisfy(country -> {
            assertThat(country.getFeaturedCity()).isNotBlank();
            assertThat(country.getImagePath()).startsWith("/images/");
        });
        assertThat(partners.findByActiveTrueAndTypeOrderByRatingDesc(PartnerType.HOTEL))
                .hasSizeGreaterThanOrEqualTo(50)
                .allSatisfy(hotel -> assertThat(hotel.getImagePath()).contains("hotels"));
    }
}
