package com.aeronest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "flight_telemetry")
public class FlightTelemetry extends BaseEntity {
    @OneToOne(fetch = FetchType.EAGER, optional = false) private Flight flight;
    @Column(precision = 9, scale = 6) private BigDecimal latitude;
    @Column(precision = 9, scale = 6) private BigDecimal longitude;
    private int altitudeFeet;
    private int groundSpeedKmh;
    private int remainingMinutes;
    private int progressPercent;
    @Column(length = 100) private String nearestLocation;
    @Column(length = 160) private String operationalNote;
    @Column(length = 80) private String sourceLabel = "AeroVista operations simulator";
    private LocalDateTime observedAt;

    public Flight getFlight() { return flight; }
    public void setFlight(Flight flight) { this.flight = flight; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public int getAltitudeFeet() { return altitudeFeet; }
    public void setAltitudeFeet(int altitudeFeet) { this.altitudeFeet = altitudeFeet; }
    public int getGroundSpeedKmh() { return groundSpeedKmh; }
    public void setGroundSpeedKmh(int groundSpeedKmh) { this.groundSpeedKmh = groundSpeedKmh; }
    public int getRemainingMinutes() { return remainingMinutes; }
    public void setRemainingMinutes(int remainingMinutes) { this.remainingMinutes = remainingMinutes; }
    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }
    public String getNearestLocation() { return nearestLocation; }
    public void setNearestLocation(String nearestLocation) { this.nearestLocation = nearestLocation; }
    public String getOperationalNote() { return operationalNote; }
    public void setOperationalNote(String operationalNote) { this.operationalNote = operationalNote; }
    public String getSourceLabel() { return sourceLabel; }
    public void setSourceLabel(String sourceLabel) { this.sourceLabel = sourceLabel; }
    public LocalDateTime getObservedAt() { return observedAt; }
    public void setObservedAt(LocalDateTime observedAt) { this.observedAt = observedAt; }
}
