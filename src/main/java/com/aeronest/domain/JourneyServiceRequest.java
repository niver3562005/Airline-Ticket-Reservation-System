package com.aeronest.domain;

import com.aeronest.domain.DomainEnums.JourneyServiceStatus;
import com.aeronest.domain.DomainEnums.JourneyServiceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "journey_service_requests")
public class JourneyServiceRequest extends BaseEntity {
    @Column(nullable = false, unique = true, length = 12) private String reference;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) private Booking booking;
    @ManyToOne(fetch = FetchType.EAGER) private Passenger passenger;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private JourneyServiceType type;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private JourneyServiceStatus status = JourneyServiceStatus.REQUESTED;
    @Column(nullable = false, length = 3) private String airportCode;
    @Column(length = 500) private String requestNotes;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal price;
    @Column(nullable = false, length = 3) private String currency = "LKR";

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public Passenger getPassenger() { return passenger; }
    public void setPassenger(Passenger passenger) { this.passenger = passenger; }
    public JourneyServiceType getType() { return type; }
    public void setType(JourneyServiceType type) { this.type = type; }
    public JourneyServiceStatus getStatus() { return status; }
    public void setStatus(JourneyServiceStatus status) { this.status = status; }
    public String getAirportCode() { return airportCode; }
    public void setAirportCode(String airportCode) { this.airportCode = airportCode; }
    public String getRequestNotes() { return requestNotes; }
    public void setRequestNotes(String requestNotes) { this.requestNotes = requestNotes; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
