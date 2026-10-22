package com.aeronest.service;

import com.aeronest.domain.Airport;
import com.aeronest.domain.Flight;
import com.aeronest.domain.FlightTelemetry;
import com.aeronest.domain.DomainEnums.FlightStatus;
import com.aeronest.repository.FlightTelemetryRepository;
import com.aeronest.web.TelemetryForm;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FlightTelemetryService {
    private final FlightTelemetryRepository telemetry;
    public FlightTelemetryService(FlightTelemetryRepository telemetry) { this.telemetry = telemetry; }

    @Transactional
    public FlightTelemetry snapshot(Flight flight) {
        return telemetry.findByFlightId(flight.getId()).orElseGet(() -> telemetry.save(initial(flight)));
    }
    @Transactional(readOnly = true)
    public List<FlightTelemetry> all() { return telemetry.findAllByOrderByObservedAtDesc(); }

    @Transactional
    public FlightTelemetry update(Flight flight, TelemetryForm form) {
        FlightTelemetry value = snapshot(flight); value.setLatitude(form.getLatitude()); value.setLongitude(form.getLongitude());
        value.setAltitudeFeet(form.getAltitudeFeet()); value.setGroundSpeedKmh(form.getGroundSpeedKmh());
        value.setRemainingMinutes(form.getRemainingMinutes()); value.setProgressPercent(form.getProgressPercent());
        value.setNearestLocation(form.getNearestLocation()); value.setOperationalNote(form.getOperationalNote());
        value.setSourceLabel("AeroVista operations control"); value.setObservedAt(LocalDateTime.now()); return value;
    }

    public TelemetryForm form(Flight flight) {
        FlightTelemetry value = snapshot(flight); TelemetryForm form = new TelemetryForm();
        form.setLatitude(value.getLatitude()); form.setLongitude(value.getLongitude()); form.setAltitudeFeet(value.getAltitudeFeet());
        form.setGroundSpeedKmh(value.getGroundSpeedKmh()); form.setRemainingMinutes(value.getRemainingMinutes());
        form.setProgressPercent(value.getProgressPercent()); form.setNearestLocation(value.getNearestLocation());
        form.setOperationalNote(value.getOperationalNote()); return form;
    }

    @Scheduled(fixedDelayString = "${aeronest.telemetry.refresh-millis:30000}")
    @Transactional
    public void advanceActiveFlights() {
        telemetry.findAllByOrderByObservedAtDesc().stream()
                .filter(value -> value.getFlight().getStatus() == FlightStatus.IN_AIR || value.getFlight().getStatus() == FlightStatus.DEPARTED)
                .forEach(this::advance);
    }

    private FlightTelemetry initial(Flight flight) {
        Airport origin = flight.getOrigin(); FlightTelemetry value = new FlightTelemetry(); value.setFlight(flight);
        value.setLatitude(origin.getLatitude() == null ? BigDecimal.ZERO : origin.getLatitude());
        value.setLongitude(origin.getLongitude() == null ? BigDecimal.ZERO : origin.getLongitude());
        value.setProgressPercent(flight.getStatus() == FlightStatus.ARRIVED ? 100 : 0);
        value.setRemainingMinutes((int) flight.getDurationMinutes()); value.setNearestLocation(origin.getCity());
        value.setOperationalNote(flight.getStatus() == FlightStatus.CANCELLED ? "Flight cancelled—passenger arrangements active" : "Awaiting departure telemetry");
        value.setObservedAt(LocalDateTime.now()); return value;
    }

    private void advance(FlightTelemetry value) {
        int progress = Math.min(100, Math.max(1, value.getProgressPercent() + 2)); value.setProgressPercent(progress);
        Airport from = value.getFlight().getOrigin(), to = value.getFlight().getDestination();
        value.setLatitude(interpolate(from.getLatitude(), to.getLatitude(), progress));
        value.setLongitude(interpolate(from.getLongitude(), to.getLongitude(), progress));
        double arc = Math.sin(Math.PI * progress / 100.0);
        value.setAltitudeFeet(progress >= 98 ? 3000 : (int) Math.round(38000 * arc));
        value.setGroundSpeedKmh(progress >= 98 ? 360 : 450 + (int) Math.round(430 * arc));
        value.setRemainingMinutes((int) Math.round(value.getFlight().getDurationMinutes() * (100 - progress) / 100.0));
        value.setNearestLocation(progress < 50 ? "En route from " + from.getCity() : "Approaching " + to.getCity());
        value.setOperationalNote(progress >= 85 ? "Descent planning in progress" : "Cruising normally");
        value.setObservedAt(LocalDateTime.now());
        if (progress >= 100) { value.getFlight().setStatus(FlightStatus.ARRIVED); value.setAltitudeFeet(0); value.setGroundSpeedKmh(0); value.setRemainingMinutes(0); value.setNearestLocation(to.getCity()); value.setOperationalNote("Landed safely"); }
    }

    private BigDecimal interpolate(BigDecimal start, BigDecimal end, int progress) {
        if (start == null || end == null) return BigDecimal.ZERO;
        return start.add(end.subtract(start).multiply(BigDecimal.valueOf(progress)).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP)).setScale(6, RoundingMode.HALF_UP);
    }
}
