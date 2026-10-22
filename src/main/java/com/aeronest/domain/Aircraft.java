package com.aeronest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class Aircraft extends BaseEntity {
    @NotBlank @Size(max = 20)
    @Column(nullable = false, unique = true)
    private String registration;
    @NotBlank @Size(max = 80)
    private String model;
    @Min(0) @Max(10)
    private int businessRows = 2;
    @Min(0) @Max(10)
    private int firstRows;
    @Min(0) @Max(15)
    private int premiumEconomyRows;
    @Min(1) @Max(50)
    private int economyRows = 20;
    @Min(4) @Max(10)
    private int seatsPerRow = 6;
    private boolean active = true;

    public int getCapacity() { return (firstRows + businessRows + premiumEconomyRows + economyRows) * seatsPerRow; }
    public String getRegistration() { return registration; }
    public void setRegistration(String registration) { this.registration = registration == null ? null : registration.toUpperCase(); }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getBusinessRows() { return businessRows; }
    public void setBusinessRows(int businessRows) { this.businessRows = businessRows; }
    public int getFirstRows() { return firstRows; }
    public void setFirstRows(int firstRows) { this.firstRows = firstRows; }
    public int getPremiumEconomyRows() { return premiumEconomyRows; }
    public void setPremiumEconomyRows(int premiumEconomyRows) { this.premiumEconomyRows = premiumEconomyRows; }
    public int getEconomyRows() { return economyRows; }
    public void setEconomyRows(int economyRows) { this.economyRows = economyRows; }
    public int getSeatsPerRow() { return seatsPerRow; }
    public void setSeatsPerRow(int seatsPerRow) { this.seatsPerRow = seatsPerRow; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
