package com.aeronest.service;

import com.aeronest.domain.ActivityLog;
import com.aeronest.domain.Booking;
import com.aeronest.domain.Flight;
import com.aeronest.domain.DomainEnums.BookingStatus;
import com.aeronest.domain.DomainEnums.PaymentStatus;
import com.aeronest.repository.ActivityLogRepository;
import com.aeronest.repository.AircraftRepository;
import com.aeronest.repository.BookingRepository;
import com.aeronest.repository.FlightRepository;
import com.aeronest.repository.PassengerRepository;
import com.aeronest.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardService {
    private final FlightRepository flights; private final BookingRepository bookings; private final PassengerRepository passengers;
    private final PaymentRepository payments; private final AircraftRepository aircraft; private final ActivityLogRepository logs;
    public DashboardService(FlightRepository flights, BookingRepository bookings, PassengerRepository passengers,
                            PaymentRepository payments, AircraftRepository aircraft, ActivityLogRepository logs) {
        this.flights = flights; this.bookings = bookings; this.passengers = passengers; this.payments = payments; this.aircraft = aircraft; this.logs = logs;
    }
    public Snapshot snapshot() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        return new Snapshot(flights.countByDepartureAtBetween(start, start.plusDays(1)), bookings.countByStatus(BookingStatus.CONFIRMED),
                bookings.countByCreatedAtAfter(LocalDateTime.now().minusDays(30)), passengers.countByCheckedInTrue(),
                payments.totalByStatus(PaymentStatus.PAID), aircraft.count(), flights.findTop8ByDepartureAtAfterOrderByDepartureAtAsc(LocalDateTime.now()),
                bookings.findAllByOrderByCreatedAtDesc().stream().limit(6).toList(), logs.findTop10ByOrderByCreatedAtDesc());
    }
    public record Snapshot(long flightsToday, long confirmedBookings, long bookingsThisMonth, long checkedIn,
                           BigDecimal revenue, long aircraft, List<Flight> upcomingFlights, List<Booking> recentBookings,
                           List<ActivityLog> activity) {}
}
