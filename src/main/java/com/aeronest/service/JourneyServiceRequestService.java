package com.aeronest.service;

import com.aeronest.domain.Booking;
import com.aeronest.domain.JourneyServiceRequest;
import com.aeronest.domain.Passenger;
import com.aeronest.domain.DomainEnums.BookingStatus;
import com.aeronest.domain.DomainEnums.JourneyServiceStatus;
import com.aeronest.domain.DomainEnums.JourneyServiceType;
import com.aeronest.repository.BookingRepository;
import com.aeronest.repository.JourneyServiceRequestRepository;
import com.aeronest.web.JourneyServiceForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class JourneyServiceRequestService {
    private final JourneyServiceRequestRepository requests;
    private final BookingRepository bookings;
    private final NotificationService notifications;

    public JourneyServiceRequestService(JourneyServiceRequestRepository requests, BookingRepository bookings,
                                        NotificationService notifications) {
        this.requests = requests; this.bookings = bookings; this.notifications = notifications;
    }

    public List<JourneyServiceRequest> forBooking(Booking booking) { return requests.findByBookingIdOrderByCreatedAtDesc(booking.getId()); }
    public List<JourneyServiceRequest> all() { return requests.findAllByOrderByCreatedAtDesc(); }

    @Transactional
    public JourneyServiceRequest request(String pnr, String email, JourneyServiceForm form) {
        Booking booking = bookings.findByPnrIgnoreCaseAndContactEmailIgnoreCase(pnr.trim(), email.trim())
                .orElseThrow(() -> new BusinessRuleException("The booking reference does not match this email"));
        if (booking.getStatus() != BookingStatus.CONFIRMED)
            throw new BusinessRuleException("Priority airport services require a confirmed booking");
        Passenger passenger = booking.getPassengers().stream()
                .filter(value -> value.getId().equals(form.getPassengerId())).findFirst()
                .orElseThrow(() -> new BusinessRuleException("Select a passenger from this booking"));
        var active = EnumSet.of(JourneyServiceStatus.REQUESTED, JourneyServiceStatus.CONFIRMED);
        if (requests.existsByBookingIdAndPassengerIdAndTypeAndStatusIn(booking.getId(), passenger.getId(), form.getType(), active))
            throw new BusinessRuleException("This service is already active for the selected passenger");
        JourneyServiceRequest request = new JourneyServiceRequest();
        request.setReference("PR-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        request.setBooking(booking); request.setPassenger(passenger); request.setType(form.getType());
        request.setAirportCode(booking.getFlight().getOrigin().getIataCode()); request.setRequestNotes(clean(form.getNotes()));
        request.setPrice(priceFor(form.getType()));
        JourneyServiceRequest saved = requests.save(request); notifications.journeyServiceRequested(saved); return saved;
    }

    @Transactional
    public void updateStatus(Long id, JourneyServiceStatus status) {
        JourneyServiceRequest request = requests.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Airport service request not found"));
        request.setStatus(status);
    }

    private BigDecimal priceFor(JourneyServiceType type) {
        return switch (type) {
            case PRIORITY_CHECK_IN -> new BigDecimal("4500.00");
            case FAST_TRACK_SECURITY -> new BigDecimal("6500.00");
            case PRIORITY_BOARDING -> new BigDecimal("3500.00");
            case BAGGAGE_ASSISTANCE -> new BigDecimal("3000.00");
            case MEET_AND_ASSIST -> new BigDecimal("8500.00");
        };
    }

    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
