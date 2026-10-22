package com.aeronest.domain;

import com.aeronest.domain.DomainEnums.FlightStatus;
import com.aeronest.domain.DomainEnums.CabinClass;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "flights")
public class Flight extends BaseEntity {
    @NotBlank @Pattern(regexp = "^[A-Za-z]{2}[0-9]{2,4}$", message = "Use a format such as AN204")
    @Column(nullable = false, length = 8)
    private String flightNumber;
    @NotNull @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(nullable = false)
    private Airport origin;
    @NotNull @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(nullable = false)
    private Airport destination;
    @NotNull @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(nullable = false)
    private Aircraft aircraft;
    @NotNull
    private LocalDateTime departureAt;
    @NotNull
    private LocalDateTime arrivalAt;
    @NotNull @Enumerated(EnumType.STRING)
    private FlightStatus status = FlightStatus.SCHEDULED;
    @NotNull @DecimalMin("1.00")
    private BigDecimal economyFare;
    @NotNull @DecimalMin("1.00")
    private BigDecimal businessFare;
    @DecimalMin("1.00")
    private BigDecimal premiumEconomyFare;
    @DecimalMin("1.00")
    private BigDecimal firstFare;
    @Min(0) @Max(60)
    private int baggageAllowanceKg = 20;
    private String departureGate;
    @Column(nullable = false, length = 40)
    private String fareBrand = "Vista Smart";
    @Column(length = 240)
    private String onboardFeatures = "Chef-inspired meal · Wi-Fi messaging · USB power";
    @Min(0) @Max(1440)
    private int delayMinutes;
    @Column(length = 240)
    private String delayReason;

    public long getDurationMinutes() { return departureAt == null || arrivalAt == null ? 0 : Duration.between(departureAt, arrivalAt).toMinutes(); }
    public String getDurationLabel() { long m = getDurationMinutes(); return (m / 60) + "h " + (m % 60) + "m"; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber == null ? null : flightNumber.toUpperCase(); }
    public Airport getOrigin() { return origin; }
    public void setOrigin(Airport origin) { this.origin = origin; }
    public Airport getDestination() { return destination; }
    public void setDestination(Airport destination) { this.destination = destination; }
    public Aircraft getAircraft() { return aircraft; }
    public void setAircraft(Aircraft aircraft) { this.aircraft = aircraft; }
    public LocalDateTime getDepartureAt() { return departureAt; }
    public void setDepartureAt(LocalDateTime departureAt) { this.departureAt = departureAt; }
    public LocalDateTime getArrivalAt() { return arrivalAt; }
    public void setArrivalAt(LocalDateTime arrivalAt) { this.arrivalAt = arrivalAt; }
    public FlightStatus getStatus() { return status; }
    public void setStatus(FlightStatus status) { this.status = status; }
    public BigDecimal getEconomyFare() { return economyFare; }
    public void setEconomyFare(BigDecimal economyFare) { this.economyFare = economyFare; }
    public BigDecimal getBusinessFare() { return businessFare; }
    public void setBusinessFare(BigDecimal businessFare) { this.businessFare = businessFare; }
    public BigDecimal getPremiumEconomyFare() { return premiumEconomyFare == null && economyFare != null ? economyFare.multiply(new BigDecimal("1.45")) : premiumEconomyFare; }
    public void setPremiumEconomyFare(BigDecimal premiumEconomyFare) { this.premiumEconomyFare = premiumEconomyFare; }
    public BigDecimal getFirstFare() { return firstFare == null && businessFare != null ? businessFare.multiply(new BigDecimal("1.85")) : firstFare; }
    public void setFirstFare(BigDecimal firstFare) { this.firstFare = firstFare; }
    public BigDecimal getFareFor(CabinClass cabin) {
        return switch (cabin) {
            case ECONOMY -> economyFare;
            case PREMIUM_ECONOMY -> getPremiumEconomyFare();
            case BUSINESS -> businessFare;
            case FIRST -> getFirstFare();
        };
    }
    public int getBaggageAllowanceKg() { return baggageAllowanceKg; }
    public void setBaggageAllowanceKg(int baggageAllowanceKg) { this.baggageAllowanceKg = baggageAllowanceKg; }
    public String getDepartureGate() { return departureGate; }
    public void setDepartureGate(String departureGate) { this.departureGate = departureGate; }
    public String getFareBrand() { return fareBrand; }
    public void setFareBrand(String fareBrand) { this.fareBrand = fareBrand; }
    public String getOnboardFeatures() { return onboardFeatures; }
    public void setOnboardFeatures(String onboardFeatures) { this.onboardFeatures = onboardFeatures; }
    public int getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(int delayMinutes) { this.delayMinutes = delayMinutes; }
    public String getDelayReason() { return delayReason; }
    public void setDelayReason(String delayReason) { this.delayReason = delayReason; }
    public LocalDateTime getEstimatedDepartureAt() { return departureAt == null ? null : departureAt.plusMinutes(delayMinutes); }
    public LocalDateTime getEstimatedArrivalAt() { return arrivalAt == null ? null : arrivalAt.plusMinutes(delayMinutes); }
    public boolean isDelayed() { return status == FlightStatus.DELAYED || delayMinutes > 0; }
    public int getBaggageAllowanceFor(CabinClass cabin) {
        return switch (cabin) {
            case ECONOMY -> baggageAllowanceKg;
            case PREMIUM_ECONOMY -> Math.min(60, baggageAllowanceKg + 7);
            case BUSINESS -> Math.min(60, baggageAllowanceKg + 17);
            case FIRST -> Math.min(60, baggageAllowanceKg + 27);
        };
    }
    public String getCabinPackage(CabinClass cabin) {
        return switch (cabin) {
            case ECONOMY -> fareBrand + " · 7 kg cabin bag · standard seat";
            case PREMIUM_ECONOMY -> "Vista Comfort · 10 kg cabin bag · extra legroom";
            case BUSINESS -> "Vista Business · lounge · priority journey · lie-flat seat";
            case FIRST -> "Vista First · private suite · chauffeur transfer · lounge";
        };
    }
}
