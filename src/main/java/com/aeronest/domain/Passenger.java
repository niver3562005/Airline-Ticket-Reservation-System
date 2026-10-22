package com.aeronest.domain;

import com.aeronest.domain.DomainEnums.CabinClass;
import com.aeronest.domain.DomainEnums.BoardingStatus;
import com.aeronest.domain.DomainEnums.SecurityStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "passengers", uniqueConstraints = @UniqueConstraint(name = "uk_flight_seat", columnNames = {"flight_id", "seat_number"}))
public class Passenger extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(nullable = false)
    private Booking booking;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(nullable = false)
    private Flight flight;
    @Column(nullable = false) private String firstName;
    @Column(nullable = false) private String lastName;
    @Column(nullable = false) private LocalDate dateOfBirth;
    @Column(nullable = false) private String nationality;
    @Column(nullable = false) private String passportNumber;
    @Column(name = "seat_number", nullable = false, length = 4) private String seatNumber;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private CabinClass cabinClass;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal fareAmount;
    private boolean checkedIn;
    private LocalDateTime checkedInAt;
    private String boardingGroup;
    @Enumerated(EnumType.STRING) private SecurityStatus securityStatus = SecurityStatus.PENDING;
    @Enumerated(EnumType.STRING) private BoardingStatus boardingStatus = BoardingStatus.NOT_READY;
    @Column(length = 80) private String mealPreference;
    @Column(length = 240) private String specialAssistance;
    @Column(length = 30) private String frequentFlyerNumber;

    public String getFullName() { return firstName + " " + lastName; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public Flight getFlight() { return flight; }
    public void setFlight(Flight flight) { this.flight = flight; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public String getPassportNumber() { return passportNumber; }
    public void setPassportNumber(String passportNumber) { this.passportNumber = passportNumber; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public CabinClass getCabinClass() { return cabinClass; }
    public void setCabinClass(CabinClass cabinClass) { this.cabinClass = cabinClass; }
    public BigDecimal getFareAmount() { return fareAmount; }
    public void setFareAmount(BigDecimal fareAmount) { this.fareAmount = fareAmount; }
    public boolean isCheckedIn() { return checkedIn; }
    public void setCheckedIn(boolean checkedIn) { this.checkedIn = checkedIn; }
    public LocalDateTime getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(LocalDateTime checkedInAt) { this.checkedInAt = checkedInAt; }
    public String getBoardingGroup() { return boardingGroup; }
    public void setBoardingGroup(String boardingGroup) { this.boardingGroup = boardingGroup; }
    public SecurityStatus getSecurityStatus() { return securityStatus == null ? SecurityStatus.PENDING : securityStatus; }
    public void setSecurityStatus(SecurityStatus securityStatus) { this.securityStatus = securityStatus; }
    public BoardingStatus getBoardingStatus() { return boardingStatus == null ? BoardingStatus.NOT_READY : boardingStatus; }
    public void setBoardingStatus(BoardingStatus boardingStatus) { this.boardingStatus = boardingStatus; }
    public String getMealPreference() { return mealPreference; }
    public void setMealPreference(String mealPreference) { this.mealPreference = mealPreference; }
    public String getSpecialAssistance() { return specialAssistance; }
    public void setSpecialAssistance(String specialAssistance) { this.specialAssistance = specialAssistance; }
    public String getFrequentFlyerNumber() { return frequentFlyerNumber; }
    public void setFrequentFlyerNumber(String frequentFlyerNumber) { this.frequentFlyerNumber = frequentFlyerNumber; }
}
