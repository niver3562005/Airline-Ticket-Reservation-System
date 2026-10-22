package com.aeronest.domain;

import com.aeronest.domain.DomainEnums.FacilityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class AirportFacility extends BaseEntity {
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(nullable = false) private Airport airport;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private FacilityType type;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, length = 500) private String description;
    @Column(length = 80) private String location;
    private boolean open24Hours;
    private boolean complimentary;

    public Airport getAirport() { return airport; }
    public void setAirport(Airport airport) { this.airport = airport; }
    public FacilityType getType() { return type; }
    public void setType(FacilityType type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public boolean isOpen24Hours() { return open24Hours; }
    public void setOpen24Hours(boolean open24Hours) { this.open24Hours = open24Hours; }
    public boolean isComplimentary() { return complimentary; }
    public void setComplimentary(boolean complimentary) { this.complimentary = complimentary; }
}
