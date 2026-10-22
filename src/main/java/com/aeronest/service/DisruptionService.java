package com.aeronest.service;

import com.aeronest.domain.Booking;
import com.aeronest.domain.DisruptionCase;
import com.aeronest.domain.Flight;
import com.aeronest.domain.Passenger;
import com.aeronest.domain.DomainEnums.BookingStatus;
import com.aeronest.domain.DomainEnums.DisruptionChoice;
import com.aeronest.domain.DomainEnums.DisruptionStatus;
import com.aeronest.domain.DomainEnums.FlightStatus;
import com.aeronest.domain.DomainEnums.BoardingStatus;
import com.aeronest.domain.DomainEnums.SecurityStatus;
import com.aeronest.repository.BookingRepository;
import com.aeronest.repository.DisruptionCaseRepository;
import com.aeronest.repository.FlightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DisruptionService {
    private final DisruptionCaseRepository cases;
    private final BookingRepository bookings;
    private final FlightRepository flights;
    private final RefundService refunds;
    private final NotificationService notifications;
    private final SeatMapService seats;

    public DisruptionService(DisruptionCaseRepository cases, BookingRepository bookings, FlightRepository flights,
                             RefundService refunds, NotificationService notifications, SeatMapService seats) {
        this.cases = cases; this.bookings = bookings; this.flights = flights; this.refunds = refunds; this.notifications = notifications;
        this.seats = seats;
    }

    @Transactional
    public void handleCancellation(Flight flight) {
        List<Flight> alternatives = alternatives(flight);
        bookings.findByFlightIdAndStatusNot(flight.getId(), BookingStatus.CANCELLED).forEach(booking -> {
            DisruptionCase disruption = cases.findByBookingId(booking.getId()).orElseGet(() -> {
                DisruptionCase value = new DisruptionCase(); value.setBooking(booking); value.setAffectedFlight(flight); return value;
            });
            disruption.setStatus(DisruptionStatus.ACTION_REQUIRED);
            String arrangement = alternatives.isEmpty()
                    ? "A full refund or travel credit is available; our service team is searching for a replacement flight."
                    : "Alternative AeroVista flights are available, with full refund and travel credit options protected.";
            disruption.setArrangementSummary(arrangement); cases.save(disruption); notifications.flightCancelled(booking, arrangement);
        });
    }

    public DisruptionCase forBooking(Booking booking) { return cases.findByBookingId(booking.getId()).orElse(null); }
    public List<DisruptionCase> all() { return cases.findAllByOrderByCreatedAtDesc(); }
    public long requiringAction() { return cases.countByStatus(DisruptionStatus.ACTION_REQUIRED); }

    public List<Flight> alternatives(Flight affected) {
        return flights.findAllByOrderByDepartureAtAsc().stream()
                .filter(value -> !value.getId().equals(affected.getId()))
                .filter(value -> value.getOrigin().getId().equals(affected.getOrigin().getId()))
                .filter(value -> value.getDestination().getId().equals(affected.getDestination().getId()))
                .filter(value -> value.getDepartureAt().isAfter(LocalDateTime.now()))
                .filter(value -> value.getStatus() == FlightStatus.SCHEDULED || value.getStatus() == FlightStatus.DELAYED)
                .limit(5).toList();
    }

    @Transactional
    public DisruptionCase select(String pnr, String email, DisruptionChoice choice, Long alternateFlightId) {
        Booking booking = bookings.findByPnrIgnoreCaseAndContactEmailIgnoreCase(pnr.trim(), email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("No booking matches that PNR and email address"));
        DisruptionCase disruption = cases.findByBookingId(booking.getId())
                .orElseThrow(() -> new BusinessRuleException("This booking does not have an active disruption case"));
        disruption.setPassengerChoice(choice); disruption.setSelectedAt(LocalDateTime.now()); disruption.setStatus(DisruptionStatus.SELECTED);
        String detail;
        if (choice == DisruptionChoice.REBOOK) {
            if (alternateFlightId == null) throw new BusinessRuleException("Choose an alternative flight");
            Flight alternate = flights.findById(alternateFlightId).orElseThrow(() -> new ResourceNotFoundException("Alternative flight not found"));
            if (!alternatives(disruption.getAffectedFlight()).stream().anyMatch(value -> value.getId().equals(alternate.getId())))
                throw new BusinessRuleException("That flight is not an eligible alternative");
            booking.getPassengers().stream().collect(java.util.stream.Collectors.groupingBy(Passenger::getCabinClass, java.util.stream.Collectors.counting()))
                    .forEach((cabin, required) -> {
                        if (seats.availableSeats(alternate, cabin).size() < required)
                            throw new BusinessRuleException("The replacement flight does not have enough " + cabin.name().replace('_', ' ').toLowerCase() + " seats");
                    });
            disruption.setAlternateFlight(alternate); detail = "Selected " + alternate.getFlightNumber() + "; operations will protect seats and confirm the change.";
        } else if (choice == DisruptionChoice.FULL_REFUND) {
            booking.setStatus(BookingStatus.CANCELLED); booking.setCancelledAt(LocalDateTime.now()); refunds.request(booking, "Airline flight cancellation");
            detail = "A full airline-disruption refund has entered processing.";
        } else {
            booking.setStatus(BookingStatus.CANCELLED); booking.setCancelledAt(LocalDateTime.now());
            detail = "Travel credit for the paid booking value will be issued by passenger care.";
        }
        disruption.setArrangementSummary(detail); notifications.disruptionChoiceSaved(booking, choice.name(), detail); return disruption;
    }

    @Transactional
    public void complete(Long id) {
        DisruptionCase disruption = cases.findById(id).orElseThrow(() -> new ResourceNotFoundException("Disruption case not found"));
        if (disruption.getPassengerChoice() == null) throw new BusinessRuleException("The passenger has not selected an arrangement");
        if (disruption.getPassengerChoice() == DisruptionChoice.REBOOK) {
            Flight alternate = disruption.getAlternateFlight();
            if (alternate == null) throw new BusinessRuleException("No replacement flight is selected");
            Booking booking = disruption.getBooking(); booking.setFlight(alternate); booking.setStatus(BookingStatus.CONFIRMED);
            java.util.Map<com.aeronest.domain.DomainEnums.CabinClass, java.util.List<String>> inventory = new java.util.EnumMap<>(com.aeronest.domain.DomainEnums.CabinClass.class);
            for (Passenger passenger : booking.getPassengers()) {
                var cabinSeats = inventory.computeIfAbsent(passenger.getCabinClass(), cabin -> new java.util.ArrayList<>(seats.availableSeats(alternate, cabin)));
                if (cabinSeats.isEmpty()) throw new BusinessRuleException("Replacement seats are no longer available");
                passenger.setFlight(alternate); passenger.setSeatNumber(cabinSeats.remove(0)); passenger.setCheckedIn(false);
                passenger.setCheckedInAt(null); passenger.setSecurityStatus(SecurityStatus.PENDING); passenger.setBoardingStatus(BoardingStatus.NOT_READY);
                passenger.setBoardingGroup(null);
            }
            disruption.setArrangementSummary("Rebooked and confirmed on " + alternate.getFlightNumber() + " with protected cabin-matched seat assignments.");
            notifications.disruptionChoiceSaved(booking, "REBOOK CONFIRMED", disruption.getArrangementSummary());
        }
        disruption.setStatus(DisruptionStatus.COMPLETED); disruption.setCompletedAt(LocalDateTime.now());
    }
}
