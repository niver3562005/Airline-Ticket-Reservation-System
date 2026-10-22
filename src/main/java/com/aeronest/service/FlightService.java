package com.aeronest.service;

import com.aeronest.domain.Flight;
import com.aeronest.domain.DomainEnums.FlightStatus;
import com.aeronest.repository.AircraftRepository;
import com.aeronest.repository.AirportRepository;
import com.aeronest.repository.FlightRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class FlightService {
    private final FlightRepository flights;
    private final AirportRepository airports;
    private final AircraftRepository aircraft;
    private final NotificationService notifications;
    private final DisruptionService disruptions;
    private final FlightTelemetryService telemetry;
    public FlightService(FlightRepository flights, AirportRepository airports, AircraftRepository aircraft,
                         NotificationService notifications, DisruptionService disruptions, FlightTelemetryService telemetry) {
        this.flights = flights; this.airports = airports; this.aircraft = aircraft; this.notifications = notifications;
        this.disruptions = disruptions; this.telemetry = telemetry;
    }
    public List<Flight> all() { return flights.findAllByOrderByDepartureAtAsc(); }
    public List<Flight> featured() { return flights.findTop8ByDepartureAtAfterOrderByDepartureAtAsc(LocalDateTime.now()).stream().limit(4).toList(); }
    public List<Flight> publicSchedule() { return flights.findAllByOrderByDepartureAtAsc().stream()
            .filter(flight -> flight.getDepartureAt().isAfter(LocalDateTime.now().minusHours(6)))
            .limit(15).toList(); }
    public Flight get(Long id) { return flights.findById(id).orElseThrow(() -> new ResourceNotFoundException("Flight not found")); }
    public List<Flight> search(Long originId, Long destinationId, LocalDate date) {
        if (originId.equals(destinationId)) throw new BusinessRuleException("Origin and destination must be different");
        return flights.search(originId, destinationId, date.atStartOfDay(), date.plusDays(1).atStartOfDay(),
                List.of(FlightStatus.SCHEDULED, FlightStatus.DELAYED));
    }
    public List<ItineraryOption> searchConnections(Long originId, Long destinationId, LocalDate date) {
        if (originId.equals(destinationId)) throw new BusinessRuleException("Origin and destination must be different");
        List<Flight> available = flights.findAllByOrderByDepartureAtAsc().stream()
                .filter(this::bookableStatus).toList();
        List<ItineraryOption> options = new ArrayList<>();
        for (Flight first : available) {
            if (!first.getOrigin().getId().equals(originId) || first.getDestination().getId().equals(destinationId)
                    || !first.getDepartureAt().toLocalDate().equals(date)) continue;
            for (Flight second : available) {
                if (!second.getOrigin().getId().equals(first.getDestination().getId()) || !second.getDestination().getId().equals(destinationId)) continue;
                long layover = Duration.between(first.getArrivalAt(), second.getDepartureAt()).toMinutes();
                if (layover < 45 || layover > 360) continue;
                long duration = Duration.between(first.getDepartureAt(), second.getArrivalAt()).toMinutes();
                options.add(new ItineraryOption(List.of(first, second), layover, duration,
                        first.getEconomyFare().add(second.getEconomyFare()),
                        first.getPremiumEconomyFare().add(second.getPremiumEconomyFare()),
                        first.getBusinessFare().add(second.getBusinessFare()), first.getFirstFare().add(second.getFirstFare())));
            }
        }
        return options.stream().sorted(Comparator.comparing(ItineraryOption::economyFare)
                .thenComparingLong(ItineraryOption::durationMinutes)).limit(5).toList();
    }
    public Flight status(String flightNumber, LocalDate date) {
        if (flightNumber == null || !flightNumber.trim().matches("(?i)^[A-Z]{2}[0-9]{2,4}$"))
            throw new BusinessRuleException("Enter a valid flight number such as AN204");
        return flights.findFirstByFlightNumberIgnoreCaseAndDepartureAtBetween(flightNumber.trim(), date.atStartOfDay(), date.plusDays(1).atStartOfDay())
                .orElseThrow(() -> new ResourceNotFoundException("No flight matches that number and date"));
    }
    @Transactional
    public Flight save(Flight flight, Long originId, Long destinationId, Long aircraftId) {
        if (originId.equals(destinationId)) throw new BusinessRuleException("Origin and destination must be different");
        if (flight.getArrivalAt() == null || flight.getDepartureAt() == null || !flight.getArrivalAt().isAfter(flight.getDepartureAt()))
            throw new BusinessRuleException("Arrival time must be after departure time");
        flight.setOrigin(airports.findById(originId).orElseThrow(() -> new ResourceNotFoundException("Origin airport not found")));
        flight.setDestination(airports.findById(destinationId).orElseThrow(() -> new ResourceNotFoundException("Destination airport not found")));
        flight.setAircraft(aircraft.findById(aircraftId).orElseThrow(() -> new ResourceNotFoundException("Aircraft not found")));
        if (Duration.between(flight.getDepartureAt(), flight.getArrivalAt()).toMinutes() < 30)
            throw new BusinessRuleException("A scheduled flight must be at least 30 minutes long");
        boolean overlap = flights.findAllByOrderByDepartureAtAsc().stream().filter(existing -> !existing.getStatus().equals(FlightStatus.CANCELLED))
                .filter(existing -> flight.getId() == null || !existing.getId().equals(flight.getId()))
                .filter(existing -> existing.getAircraft().getId().equals(aircraftId))
                .anyMatch(existing -> flight.getDepartureAt().isBefore(existing.getArrivalAt().plusMinutes(60))
                        && flight.getArrivalAt().plusMinutes(60).isAfter(existing.getDepartureAt()));
        if (overlap) throw new BusinessRuleException("This aircraft has an overlapping flight or insufficient 60-minute turnaround time");
        return flights.save(flight);
    }
    @Transactional public void updateStatus(Long id, FlightStatus status) {
        Flight flight = get(id); flight.setStatus(status);
        if (status == FlightStatus.DELAYED && flight.getDelayMinutes() == 0) {
            flight.setDelayMinutes(45); flight.setDelayReason("Operational schedule adjustment");
        } else if (status != FlightStatus.DELAYED && status != FlightStatus.CANCELLED) {
            flight.setDelayMinutes(0); flight.setDelayReason(null);
        }
        if (status == FlightStatus.CANCELLED) disruptions.handleCancellation(flight);
        if (status == FlightStatus.DEPARTED || status == FlightStatus.IN_AIR || status == FlightStatus.ARRIVED) telemetry.snapshot(flight);
        notifications.flightStatusChanged(flight, status);
    }
    @Transactional public void delete(Long id) {
        try { flights.delete(get(id)); flights.flush(); }
        catch (DataIntegrityViolationException ex) { throw new BusinessRuleException("A flight with bookings cannot be deleted. Cancel it instead."); }
    }
    private boolean bookableStatus(Flight flight) { return flight.getStatus() == FlightStatus.SCHEDULED || flight.getStatus() == FlightStatus.DELAYED; }

    public record ItineraryOption(List<Flight> segments, long layoverMinutes, long durationMinutes,
                                  BigDecimal economyFare, BigDecimal premiumEconomyFare, BigDecimal businessFare, BigDecimal firstFare) {
        public Flight first() { return segments.get(0); }
        public Flight second() { return segments.get(1); }
        public String durationLabel() { return (durationMinutes / 60) + "h " + (durationMinutes % 60) + "m"; }
        public String layoverLabel() { return (layoverMinutes / 60) + "h " + (layoverMinutes % 60) + "m"; }
        public Flight getFirst() { return first(); }
        public Flight getSecond() { return second(); }
        public String getDurationLabel() { return durationLabel(); }
        public String getLayoverLabel() { return layoverLabel(); }
        public BigDecimal fareFor(com.aeronest.domain.DomainEnums.CabinClass cabin) {
            return switch (cabin) { case ECONOMY -> economyFare; case PREMIUM_ECONOMY -> premiumEconomyFare; case BUSINESS -> businessFare; case FIRST -> firstFare; };
        }
    }
}
