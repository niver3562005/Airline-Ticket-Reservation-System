package com.aeronest.service;

import com.aeronest.domain.Booking;
import com.aeronest.domain.CustomerAccount;
import com.aeronest.domain.PartnerReservation;
import com.aeronest.domain.TravelPartner;
import com.aeronest.domain.DomainEnums.PartnerReservationStatus;
import com.aeronest.domain.DomainEnums.PartnerType;
import com.aeronest.repository.BookingRepository;
import com.aeronest.repository.PartnerReservationRepository;
import com.aeronest.repository.TravelPartnerRepository;
import com.aeronest.web.PartnerReservationForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TravelPartnerService {
    private final TravelPartnerRepository partners;
    private final PartnerReservationRepository reservations;
    private final BookingRepository bookings;
    private final NotificationService notifications;

    public TravelPartnerService(TravelPartnerRepository partners, PartnerReservationRepository reservations,
                                BookingRepository bookings, NotificationService notifications) {
        this.partners = partners; this.reservations = reservations; this.bookings = bookings; this.notifications = notifications;
    }

    public List<TravelPartner> partners(PartnerType type) {
        return type == null ? partners.findByActiveTrueOrderByTypeAscRatingDesc() : partners.findByActiveTrueAndTypeOrderByRatingDesc(type);
    }
    public TravelPartner partner(Long id) { return partners.findById(id).orElseThrow(() -> new ResourceNotFoundException("Travel service not found")); }
    public List<PartnerReservation> allReservations() { return reservations.findAllByOrderByCreatedAtDesc(); }
    public List<PartnerReservation> forAccount(CustomerAccount account) { return reservations.findByAccountIdOrderByCreatedAtDesc(account.getId()); }

    @Transactional
    public PartnerReservation reserve(PartnerReservationForm form, CustomerAccount account) {
        TravelPartner partner = partner(form.getPartnerId());
        if (!partner.isActive()) throw new BusinessRuleException("This service is currently unavailable");
        if (form.getServiceEndsAt() != null && !form.getServiceEndsAt().isAfter(form.getServiceAt()))
            throw new BusinessRuleException("The checkout or end time must be after the start time");
        Booking booking = null;
        if (form.getAirlinePnr() != null && !form.getAirlinePnr().isBlank()) {
            booking = bookings.findByPnrIgnoreCaseAndContactEmailIgnoreCase(form.getAirlinePnr().trim(), form.getEmail().trim())
                    .orElseThrow(() -> new BusinessRuleException("The airline booking reference does not match this email"));
        }
        PartnerReservation reservation = new PartnerReservation();
        reservation.setReference("SV-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        reservation.setPartner(partner); reservation.setAccount(account); reservation.setAirlineBooking(booking);
        reservation.setTravellerName(form.getTravellerName().trim()); reservation.setEmail(form.getEmail().trim().toLowerCase());
        reservation.setPhone(form.getPhone().trim()); reservation.setServiceAt(form.getServiceAt()); reservation.setServiceEndsAt(form.getServiceEndsAt());
        reservation.setGuests(form.getGuests()); reservation.setPickupLocation(form.getPickupLocation()); reservation.setDropoffLocation(form.getDropoffLocation());
        reservation.setNotes(form.getNotes()); reservation.setEstimatedTotal(estimate(partner, form)); reservation.setCurrency(partner.getCurrency());
        PartnerReservation saved = reservations.save(reservation); notifications.partnerReservationCreated(saved); return saved;
    }

    @Transactional
    public void updateStatus(Long id, PartnerReservationStatus status) {
        PartnerReservation reservation = reservations.findById(id).orElseThrow(() -> new ResourceNotFoundException("Service reservation not found"));
        reservation.setStatus(status);
    }

    private BigDecimal estimate(TravelPartner partner, PartnerReservationForm form) {
        BigDecimal base = partner.getPriceFrom() == null ? BigDecimal.ZERO : partner.getPriceFrom();
        long units = partner.getType() == PartnerType.HOTEL && form.getServiceEndsAt() != null
                ? Math.max(1, java.time.Duration.between(form.getServiceAt(), form.getServiceEndsAt()).toDays()) : 1;
        return base.multiply(BigDecimal.valueOf(units)).multiply(BigDecimal.valueOf(Math.max(1, form.getGuests())));
    }
}
