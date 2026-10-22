package com.aeronest.config;

import com.aeronest.domain.Flight;
import com.aeronest.domain.DomainEnums.FlightStatus;
import com.aeronest.repository.FlightRepository;
import com.aeronest.service.FlightTelemetryService;
import com.aeronest.web.TelemetryForm;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Order(4)
public class TelemetrySeeder implements CommandLineRunner {
    private final FlightRepository flights;
    private final FlightTelemetryService telemetry;

    public TelemetrySeeder(FlightRepository flights, FlightTelemetryService telemetry) {
        this.flights = flights; this.telemetry = telemetry;
    }

    @Override @Transactional public void run(String... args) {
        flights.findAllByOrderByDepartureAtAsc().forEach(telemetry::snapshot);
        Flight live = flights.findFirstByFlightNumberIgnoreCase("AN900").orElse(null);
        if (live != null) {
            if (live.getDepartureAt().toLocalDate().isBefore(LocalDate.now())) {
                LocalDateTime departure = LocalDateTime.now().minusHours(2);
                live.setDepartureAt(departure); live.setArrivalAt(departure.plusHours(4).plusMinutes(5));
            }
            live.setStatus(FlightStatus.IN_AIR);
            var value = telemetry.snapshot(live);
            if (value.getProgressPercent() == 0 || value.getProgressPercent() >= 100 ||
                    value.getUpdatedAt().toLocalDate().isBefore(LocalDate.now())) {
                TelemetryForm form = new TelemetryForm(); form.setLatitude(new java.math.BigDecimal("5.824100"));
                form.setLongitude(new java.math.BigDecimal("91.943600")); form.setProgressPercent(48);
                form.setAltitudeFeet(37000); form.setGroundSpeedKmh(842); form.setRemainingMinutes(128);
                form.setNearestLocation("Bay of Bengal"); form.setOperationalNote("Cruising normally · automatic operations feed");
                telemetry.update(live, form);
            }
        }
    }
}
