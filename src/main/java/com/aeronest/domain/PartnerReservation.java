package com.aeronest.domain;

import com.aeronest.domain.DomainEnums.PartnerReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "partner_reservations")
public class PartnerReservation extends BaseEntity {
    @Column(nullable = false, unique = true, length = 12) private String reference;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) private TravelPartner partner;
    @ManyToOne(fetch = FetchType.LAZY) private CustomerAccount account;
    @ManyToOne(fetch = FetchType.LAZY) private Booking airlineBooking;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private PartnerReservationStatus status = PartnerReservationStatus.REQUESTED;
    @Column(nullable = false, length = 120) private String travellerName;
    @Column(nullable = false, length = 160) private String email;
    @Column(nullable = false, length = 40) private String phone;
    @Column(nullable = false) private LocalDateTime serviceAt;
    private LocalDateTime serviceEndsAt;
    private int guests = 1;
    @Column(length = 180) private String pickupLocation;
    @Column(length = 180) private String dropoffLocation;
    @Column(length = 500) private String notes;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal estimatedTotal;
    @Column(nullable = false, length = 3) private String currency = "LKR";

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public TravelPartner getPartner() { return partner; }
    public void setPartner(TravelPartner partner) { this.partner = partner; }
    public CustomerAccount getAccount() { return account; }
    public void setAccount(CustomerAccount account) { this.account = account; }
    public Booking getAirlineBooking() { return airlineBooking; }
    public void setAirlineBooking(Booking airlineBooking) { this.airlineBooking = airlineBooking; }
    public PartnerReservationStatus getStatus() { return status; }
    public void setStatus(PartnerReservationStatus status) { this.status = status; }
    public String getTravellerName() { return travellerName; }
    public void setTravellerName(String travellerName) { this.travellerName = travellerName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDateTime getServiceAt() { return serviceAt; }
    public void setServiceAt(LocalDateTime serviceAt) { this.serviceAt = serviceAt; }
    public LocalDateTime getServiceEndsAt() { return serviceEndsAt; }
    public void setServiceEndsAt(LocalDateTime serviceEndsAt) { this.serviceEndsAt = serviceEndsAt; }
    public int getGuests() { return guests; }
    public void setGuests(int guests) { this.guests = guests; }
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    public String getDropoffLocation() { return dropoffLocation; }
    public void setDropoffLocation(String dropoffLocation) { this.dropoffLocation = dropoffLocation; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public BigDecimal getEstimatedTotal() { return estimatedTotal; }
    public void setEstimatedTotal(BigDecimal estimatedTotal) { this.estimatedTotal = estimatedTotal; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
