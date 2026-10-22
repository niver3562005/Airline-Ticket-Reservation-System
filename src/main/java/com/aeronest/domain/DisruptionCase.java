package com.aeronest.domain;

import com.aeronest.domain.DomainEnums.DisruptionChoice;
import com.aeronest.domain.DomainEnums.DisruptionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "disruption_cases")
public class DisruptionCase extends BaseEntity {
    @OneToOne(fetch = FetchType.EAGER, optional = false) private Booking booking;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) private Flight affectedFlight;
    @ManyToOne(fetch = FetchType.EAGER) private Flight alternateFlight;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private DisruptionStatus status = DisruptionStatus.ACTION_REQUIRED;
    @Enumerated(EnumType.STRING) @Column(length = 24) private DisruptionChoice passengerChoice;
    @Column(nullable = false, length = 500) private String arrangementSummary;
    private LocalDateTime selectedAt;
    private LocalDateTime completedAt;

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public Flight getAffectedFlight() { return affectedFlight; }
    public void setAffectedFlight(Flight affectedFlight) { this.affectedFlight = affectedFlight; }
    public Flight getAlternateFlight() { return alternateFlight; }
    public void setAlternateFlight(Flight alternateFlight) { this.alternateFlight = alternateFlight; }
    public DisruptionStatus getStatus() { return status; }
    public void setStatus(DisruptionStatus status) { this.status = status; }
    public DisruptionChoice getPassengerChoice() { return passengerChoice; }
    public void setPassengerChoice(DisruptionChoice passengerChoice) { this.passengerChoice = passengerChoice; }
    public String getArrangementSummary() { return arrangementSummary; }
    public void setArrangementSummary(String arrangementSummary) { this.arrangementSummary = arrangementSummary; }
    public LocalDateTime getSelectedAt() { return selectedAt; }
    public void setSelectedAt(LocalDateTime selectedAt) { this.selectedAt = selectedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
