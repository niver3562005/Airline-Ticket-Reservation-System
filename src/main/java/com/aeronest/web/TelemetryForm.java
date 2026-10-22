package com.aeronest.web;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class TelemetryForm {
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") private BigDecimal latitude;
    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") private BigDecimal longitude;
    @Min(0) @Max(50000) private int altitudeFeet;
    @Min(0) @Max(1200) private int groundSpeedKmh;
    @Min(0) @Max(1800) private int remainingMinutes;
    @Min(0) @Max(100) private int progressPercent;
    @NotBlank @Size(max = 100) private String nearestLocation;
    @NotBlank @Size(max = 160) private String operationalNote;

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
}
