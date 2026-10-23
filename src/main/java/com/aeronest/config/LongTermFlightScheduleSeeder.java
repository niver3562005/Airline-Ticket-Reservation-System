package com.aeronest.config;

import com.aeronest.domain.Aircraft;
import com.aeronest.domain.Airport;
import com.aeronest.domain.Flight;
import com.aeronest.domain.DomainEnums.FlightStatus;
import com.aeronest.repository.AircraftRepository;
import com.aeronest.repository.AirportRepository;
import com.aeronest.repository.FlightRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * Publishes a rolling, database-backed long-term timetable for every AeroVista
 * destination. Each route receives 25 outbound and 25 return services across
 * roughly one year. Existing flight/date combinations are retained so this is
 * safe to run repeatedly against MySQL.
 */
@Component
@Order(3)
public class LongTermFlightScheduleSeeder implements CommandLineRunner {
    private static final int ROUND_TRIPS_PER_DESTINATION = 25;

    private final AirportRepository airports;
    private final AircraftRepository aircraft;
    private final FlightRepository flights;

    public LongTermFlightScheduleSeeder(AirportRepository airports, AircraftRepository aircraft,
                                        FlightRepository flights) {
        this.airports = airports;
        this.aircraft = aircraft;
        this.flights = flights;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (airports.count() == 0 || aircraft.count() == 0) return;

        LocalDate firstScheduleWeek = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        for (RouteSchedule route : routeSchedules()) {
            for (int journey = 0; journey < ROUND_TRIPS_PER_DESTINATION; journey++) {
                LocalDate scheduleWeek = firstScheduleWeek.plusWeeks(journey * 2L);
                int timeVariant = journey % route.outboundTimes().size();

                publish(route.outboundNumber(), "CMB", route.destinationCode(), route.aircraftRegistration(),
                        scheduleWeek.plusDays(route.outboundDayOffset()), route.outboundTimes().get(timeVariant),
                        route.durationHours(), route.durationMinutes(), route.outboundGate(), route.economyFare(),
                        route.businessFare(), route.baggageAllowanceKg(), route.fareBrand(), route.onboardFeatures());

                publish(route.returnNumber(), route.destinationCode(), "CMB", route.aircraftRegistration(),
                        scheduleWeek.plusDays(route.returnDayOffset()), route.returnTimes().get(timeVariant),
                        route.returnDurationHours(), route.returnDurationMinutes(), route.returnGate(),
                        route.economyFare() + route.returnFareAdjustment(),
                        route.businessFare() + route.returnFareAdjustment() * 2,
                        route.baggageAllowanceKg(), route.fareBrand(), route.onboardFeatures());
            }
        }
    }

    private void publish(String flightNumber, String originCode, String destinationCode, String aircraftRegistration,
                         LocalDate date, String time, int hours, int minutes, String gate, int economyFare,
                         int businessFare, int baggageAllowanceKg, String fareBrand, String onboardFeatures) {
        if (flights.findFirstByFlightNumberIgnoreCaseAndDepartureAtBetween(
                flightNumber, date.atStartOfDay(), date.plusDays(1).atStartOfDay()).isPresent()) return;

        LocalDateTime departure = LocalDateTime.of(date, LocalTime.parse(time));
        Flight flight = new Flight();
        flight.setFlightNumber(flightNumber);
        flight.setOrigin(airport(originCode));
        flight.setDestination(airport(destinationCode));
        flight.setAircraft(plane(aircraftRegistration));
        flight.setDepartureAt(departure);
        flight.setArrivalAt(departure.plusHours(hours).plusMinutes(minutes));
        flight.setDepartureGate(gate);
        flight.setEconomyFare(BigDecimal.valueOf(economyFare));
        flight.setPremiumEconomyFare(BigDecimal.valueOf(economyFare).multiply(new BigDecimal("1.45")));
        flight.setBusinessFare(BigDecimal.valueOf(businessFare));
        flight.setFirstFare(BigDecimal.valueOf(businessFare).multiply(new BigDecimal("1.85")));
        flight.setBaggageAllowanceKg(baggageAllowanceKg);
        flight.setFareBrand(fareBrand);
        flight.setOnboardFeatures(onboardFeatures);
        flight.setStatus(FlightStatus.SCHEDULED);
        flights.save(flight);
    }

    /** Manually curated operating patterns for all seven international destinations. */
    private List<RouteSchedule> routeSchedules() {
        return List.of(
                new RouteSchedule("DXB", "AN820", "AN821", "4R-ANC", 1, 2,
                        List.of("02:10", "04:40", "18:25"), List.of("09:15", "18:10", "22:35"),
                        4, 40, 4, 35, "4", "B12", 89500, 2200, 238000, 30,
                        "Vista Flex", "Chef-inspired meal · full Wi-Fi · seat power · Arabic entertainment"),
                new RouteSchedule("SIN", "AN830", "AN831", "4R-ANB", 0, 1,
                        List.of("07:35", "10:15", "21:05"), List.of("08:20", "16:45", "21:20"),
                        4, 5, 4, 0, "7", "D4", 74200, 2300, 189000, 25,
                        "Vista Smart", "Asian dining · Wi-Fi messaging · USB power · streaming entertainment"),
                new RouteSchedule("MLE", "AN840", "AN841", "4R-ANA", 1, 1,
                        List.of("06:30", "10:20", "14:15"), List.of("09:10", "16:05", "19:35"),
                        1, 30, 1, 25, "2", "1", 38900, 1200, 92000, 20,
                        "Vista Saver", "Light meal · island transfer guidance · USB power"),
                new RouteSchedule("DEL", "AN850", "AN851", "4R-ANB", 2, 3,
                        List.of("06:15", "14:45", "20:30"), List.of("06:50", "12:25", "22:10"),
                        3, 35, 3, 30, "9", "14", 56200, 1700, 141000, 25,
                        "Vista Smart", "Regional dining · Wi-Fi messaging · USB power · entertainment library"),
                new RouteSchedule("BKK", "AN860", "AN861", "4R-ANB", 4, 5,
                        List.of("01:30", "09:40", "17:55"), List.of("03:20", "11:40", "19:15"),
                        3, 45, 3, 50, "5", "C8", 68100, 1600, 169000, 25,
                        "Vista Flex", "Thai and Sri Lankan dining · Wi-Fi messaging · seat power"),
                new RouteSchedule("KUL", "AN870", "AN871", "4R-ANA", 3, 4,
                        List.of("08:15", "13:30", "22:05"), List.of("02:20", "09:45", "18:30"),
                        3, 40, 3, 35, "6", "H9", 62400, 1500, 154000, 20,
                        "Vista Saver", "Hot meal · Wi-Fi messaging · USB power · family entertainment"),
                new RouteSchedule("DOH", "AN880", "AN881", "4R-ANC", 4, 5,
                        List.of("03:25", "11:20", "20:10"), List.of("02:45", "16:30", "23:15"),
                        5, 0, 4, 50, "3", "C14", 92700, 2400, 246000, 30,
                        "Vista Signature", "Premium dining · full Wi-Fi · seat power · lounge eligibility")
        );
    }

    private Airport airport(String code) {
        return airports.findByIataCodeIgnoreCase(code).orElseThrow();
    }

    private Aircraft plane(String registration) {
        return aircraft.findByRegistrationIgnoreCase(registration).orElseThrow();
    }

    private record RouteSchedule(
            String destinationCode, String outboundNumber, String returnNumber, String aircraftRegistration,
            int outboundDayOffset, int returnDayOffset, List<String> outboundTimes, List<String> returnTimes,
            int durationHours, int durationMinutes, int returnDurationHours, int returnDurationMinutes,
            String outboundGate, String returnGate, int economyFare, int returnFareAdjustment,
            int businessFare, int baggageAllowanceKg, String fareBrand, String onboardFeatures) {
    }
}
