package com.aeronest.service;

import com.aeronest.domain.Booking;
import com.aeronest.domain.Flight;
import com.aeronest.domain.Passenger;
import com.aeronest.domain.Payment;
import com.aeronest.domain.DomainEnums.BookingStatus;
import com.aeronest.domain.DomainEnums.CabinClass;
import com.aeronest.domain.DomainEnums.FlightStatus;
import com.aeronest.domain.DomainEnums.PaymentStatus;
import com.aeronest.domain.DomainEnums.BoardingStatus;
import com.aeronest.domain.DomainEnums.SecurityStatus;
import com.aeronest.repository.BookingRepository;
import com.aeronest.repository.FlightRepository;
import com.aeronest.repository.PassengerRepository;
import com.aeronest.repository.PaymentRepository;
import com.aeronest.web.BookingForm;
import com.aeronest.web.PassengerForm;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional(readOnly = true)
public class BookingService {
    private final BookingRepository bookings; private final FlightRepository flights; private final PassengerRepository passengers;
    private final PaymentRepository payments; private final SeatMapService seats; private final PricingStrategy pricing;
    private final PaymentProcessorFactory processors; private final ApplicationEventPublisher events;
    private final NotificationService notifications; private final RefundService refunds;
    private final PromotionCatalogService promotions;
    public BookingService(BookingRepository bookings, FlightRepository flights, PassengerRepository passengers,
                          PaymentRepository payments, SeatMapService seats, PricingStrategy pricing,
                          PaymentProcessorFactory processors, ApplicationEventPublisher events,
                          NotificationService notifications, RefundService refunds, PromotionCatalogService promotions) {
        this.bookings = bookings; this.flights = flights; this.passengers = passengers; this.payments = payments;
        this.seats = seats; this.pricing = pricing; this.processors = processors; this.events = events;
        this.notifications = notifications; this.refunds = refunds;
        this.promotions = promotions;
    }
    public List<Booking> all() { return bookings.findAllByOrderByCreatedAtDesc(); }
    public Booking get(Long id) { return bookings.findById(id).orElseThrow(() -> new ResourceNotFoundException("Booking not found")); }
    public Booking find(String pnr, String email) { return bookings.findByPnrIgnoreCaseAndContactEmailIgnoreCase(pnr.trim(), email.trim())
            .orElseThrow(() -> new ResourceNotFoundException("No booking matches that PNR and email address")); }
    public Booking findByPnr(String pnr) { return bookings.findByPnrIgnoreCase(pnr).orElseThrow(() -> new ResourceNotFoundException("Booking not found")); }

    @Transactional
    public Booking create(BookingForm form) {
        Flight flight = flights.findById(form.getFlightId()).orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
        if (flight.getStatus() == FlightStatus.CANCELLED || !flight.getDepartureAt().isAfter(LocalDateTime.now()))
            throw new BusinessRuleException("This flight is no longer open for booking");
        if (form.getPassengers().isEmpty() || form.getPassengers().size() > 6) throw new BusinessRuleException("A booking must contain 1 to 6 travellers");
        if (form.getPromoCode() != null && !form.getPromoCode().isBlank() && promotions.find(form.getPromoCode()).isEmpty())
            throw new BusinessRuleException("That promo code is not valid or has expired. Visit Deals for current offers.");
        Set<String> chosen = new HashSet<>();
        for (PassengerForm p : form.getPassengers()) {
            String seat = p.getSeatNumber().toUpperCase();
            if (!chosen.add(seat)) throw new BusinessRuleException("Each traveller must have a different seat");
            if (passengers.existsByFlightIdAndSeatNumberIgnoreCaseAndBookingStatusNot(flight.getId(), seat, BookingStatus.CANCELLED))
                throw new BusinessRuleException("Seat " + seat + " was just taken. Please choose another seat.");
            if (seats.cabinFor(flight, seat) != p.getCabinClass()) throw new BusinessRuleException("Seat " + seat + " does not belong to " + p.getCabinClass() + " cabin");
        }
        Booking booking = new Booking();
        booking.setPnr(uniquePnr()); booking.setFlight(flight); booking.setContactName(form.getContactName());
        booking.setContactEmail(form.getContactEmail().toLowerCase()); booking.setContactPhone(form.getContactPhone());
        booking.setPromoCode(form.getPromoCode() == null ? null : form.getPromoCode().toUpperCase());
        long occupied = passengers.countByFlightId(flight.getId());
        BigDecimal total = BigDecimal.ZERO;
        for (PassengerForm p : form.getPassengers()) {
            Passenger passenger = new Passenger(); passenger.setFirstName(p.getFirstName()); passenger.setLastName(p.getLastName());
            passenger.setDateOfBirth(p.getDateOfBirth()); passenger.setNationality(p.getNationality());
            passenger.setPassportNumber(p.getPassportNumber().toUpperCase()); passenger.setSeatNumber(p.getSeatNumber().toUpperCase());
            passenger.setCabinClass(p.getCabinClass());
            passenger.setMealPreference(p.getMealPreference()); passenger.setSpecialAssistance(p.getSpecialAssistance());
            passenger.setFrequentFlyerNumber(p.getFrequentFlyerNumber());
            BigDecimal fare = pricing.calculate(flight, p.getCabinClass(), occupied++, form.getPromoCode());
            passenger.setFareAmount(fare); total = total.add(fare); booking.addPassenger(passenger);
        }
        booking.setTotalAmount(total);
        PaymentProcessor.PaymentResult result = processors.forMethod(form.getPaymentMethod()).process(total);
        booking.setStatus(result.status() == PaymentStatus.PAID ? BookingStatus.CONFIRMED : BookingStatus.PENDING);
        if (booking.getStatus() == BookingStatus.CONFIRMED) booking.setConfirmedAt(LocalDateTime.now());
        try { bookings.saveAndFlush(booking); }
        catch (DataIntegrityViolationException ex) { throw new BusinessRuleException("One of the selected seats is no longer available"); }
        Payment payment = new Payment(); payment.setBooking(booking); payment.setAmount(total); payment.setMethod(form.getPaymentMethod());
        payment.setStatus(result.status()); payment.setReference(result.reference()); payment.setProcessedAt(LocalDateTime.now()); payments.save(payment);
        events.publishEvent(new BookingEvent("BOOKING_CREATED", booking.getPnr(), form.getPassengers().size() + " traveller(s) booked on " + flight.getFlightNumber()));
        notifications.bookingCreated(booking);
        return booking;
    }

    @Transactional
    public void cancel(String pnr, String email) { cancel(pnr, email, "Travel plans changed"); }

    @Transactional
    public void cancel(String pnr, String email, String reason) {
        Booking booking = find(pnr, email);
        if (booking.getStatus() == BookingStatus.CANCELLED) throw new BusinessRuleException("This booking is already cancelled");
        if (!booking.getFlight().getDepartureAt().isAfter(LocalDateTime.now().plusHours(4))) throw new BusinessRuleException("Online cancellation closes four hours before departure");
        booking.setStatus(BookingStatus.CANCELLED); booking.setCancelledAt(LocalDateTime.now());
        refunds.request(booking, reason);
        events.publishEvent(new BookingEvent("BOOKING_CANCELLED", booking.getPnr(), "Booking cancelled and refund workflow started"));
    }

    @Transactional
    public Passenger checkIn(String pnr, String email, Long passengerId) {
        Booking booking = find(pnr, email);
        if (booking.getStatus() != BookingStatus.CONFIRMED) throw new BusinessRuleException("Only confirmed bookings can check in");
        LocalDateTime departure = booking.getFlight().getDepartureAt();
        if (LocalDateTime.now().isBefore(departure.minusHours(48))) throw new BusinessRuleException("Online check-in opens 48 hours before departure");
        if (!LocalDateTime.now().isBefore(departure)) throw new BusinessRuleException("This flight has already departed");
        Passenger passenger = booking.getPassengers().stream().filter(p -> p.getId().equals(passengerId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found in this booking"));
        passenger.setCheckedIn(true); passenger.setCheckedInAt(LocalDateTime.now());
        passenger.setSecurityStatus(SecurityStatus.DOCUMENTS_VERIFIED); passenger.setBoardingStatus(BoardingStatus.READY);
        passenger.setBoardingGroup(passenger.getCabinClass() == CabinClass.FIRST ? "First" :
                passenger.getCabinClass() == CabinClass.BUSINESS ? "Priority" : "Group " + (((passenger.getSeatNumber().charAt(passenger.getSeatNumber().length() - 1) - 'A') % 3) + 1));
        events.publishEvent(new BookingEvent("PASSENGER_CHECKED_IN", booking.getPnr(), passenger.getFullName() + " checked in for seat " + passenger.getSeatNumber()));
        notifications.checkInCompleted(booking, passenger.getFullName(), passenger.getBoardingGroup());
        return passenger;
    }

    @Transactional
    public void updatePassengerOperations(Long passengerId, SecurityStatus securityStatus, BoardingStatus boardingStatus) {
        Passenger passenger = passengers.findById(passengerId).orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));
        if (boardingStatus == BoardingStatus.BOARDED && !passenger.isCheckedIn())
            throw new BusinessRuleException("A passenger must check in before boarding");
        if (boardingStatus == BoardingStatus.BOARDED && securityStatus != SecurityStatus.CLEARED)
            throw new BusinessRuleException("Security clearance is required before boarding");
        passenger.setSecurityStatus(securityStatus); passenger.setBoardingStatus(boardingStatus);
        notifications.passengerOperationalUpdate(passenger.getBooking(), passenger.getFullName(), securityStatus.name(), boardingStatus.name());
        events.publishEvent(new BookingEvent("PASSENGER_STATUS_UPDATED", passenger.getBooking().getPnr(),
                passenger.getFullName() + " security " + securityStatus + ", boarding " + boardingStatus));
    }

    private String uniquePnr() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        String value;
        do { StringBuilder b = new StringBuilder(); for (int i = 0; i < 6; i++) b.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length()))); value = b.toString(); }
        while (bookings.findByPnrIgnoreCase(value).isPresent());
        return value;
    }
}
