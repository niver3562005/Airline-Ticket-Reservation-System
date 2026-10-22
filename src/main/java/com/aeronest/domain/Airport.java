package com.aeronest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@Table(name = "airports")
public class Airport extends BaseEntity {
    @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$", message = "Use a three-letter IATA code")
    @Column(nullable = false, unique = true, length = 3)
    private String iataCode;
    @NotBlank @Size(max = 120)
    private String name;
    @NotBlank @Size(max = 80)
    private String city;
    @NotBlank @Size(max = 80)
    private String country;
    @NotBlank @Size(max = 60)
    private String timeZone;
    @Column(precision = 9, scale = 6) private BigDecimal latitude;
    @Column(precision = 9, scale = 6) private BigDecimal longitude;
    private boolean active = true;

    public String getIataCode() { return iataCode; }
    public void setIataCode(String iataCode) { this.iataCode = iataCode == null ? null : iataCode.toUpperCase(); }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getDisplayName() { return city + " (" + iataCode + ")"; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
}
