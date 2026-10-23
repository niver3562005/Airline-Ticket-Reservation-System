package com.aeronest.service;

import com.aeronest.domain.Flight;
import com.aeronest.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LongTermFlightScheduleIntegrationTest {
    private static final Map<String, Set<String>> ROUTE_NUMBERS = Map.of(
            "DXB", Set.of("AN820", "AN821"),
            "SIN", Set.of("AN830", "AN831"),
            "MLE", Set.of("AN840", "AN841"),
            "DEL", Set.of("AN850", "AN851"),
            "BKK", Set.of("AN860", "AN861"),
            "KUL", Set.of("AN870", "AN871"),
            "DOH", Set.of("AN880", "AN881")
    );

    @Autowired
    private FlightRepository flights;

    @Test
    void publishesFiftyFlightsForEveryInternationalDestination() {
        List<Flight> schedule = flights.findAllByOrderByDepartureAtAsc();

        ROUTE_NUMBERS.forEach((destination, numbers) -> {
            List<Flight> routeFlights = schedule.stream()
                    .filter(flight -> numbers.contains(flight.getFlightNumber()))
                    .toList();

            assertThat(routeFlights).hasSize(50);
            assertThat(routeFlights).extracting(flight -> flight.getOrigin().getIataCode())
                    .contains("CMB", destination);
            assertThat(routeFlights).extracting(flight -> flight.getDestination().getIataCode())
                    .contains("CMB", destination);
        });
    }

    @Test
    void scheduleSpansAlmostOneYearAndRotatesDepartureTimes() {
        List<Flight> longTermFlights = flights.findAllByOrderByDepartureAtAsc().stream()
                .filter(flight -> ROUTE_NUMBERS.values().stream().anyMatch(numbers -> numbers.contains(flight.getFlightNumber())))
                .toList();

        assertThat(Duration.between(longTermFlights.get(0).getDepartureAt(),
                longTermFlights.get(longTermFlights.size() - 1).getDepartureAt()).toDays()).isGreaterThanOrEqualTo(330);
        assertThat(longTermFlights.stream().map(flight -> flight.getDepartureAt().toLocalTime())
                .collect(Collectors.toSet()).size()).isGreaterThanOrEqualTo(20);
        assertThat(longTermFlights).allSatisfy(flight -> {
            assertThat(flight.getEconomyFare()).isPositive();
            assertThat(flight.getBusinessFare()).isGreaterThan(flight.getEconomyFare());
            assertThat(flight.getBaggageAllowanceKg()).isBetween(20, 30);
            assertThat(flight.getDepartureAt()).isBefore(flight.getArrivalAt());
        });
    }
}
