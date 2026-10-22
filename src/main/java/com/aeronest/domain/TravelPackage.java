package com.aeronest.domain;

import com.aeronest.domain.DomainEnums.PackageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;

@Entity
public class TravelPackage extends BaseEntity {
    @Column(nullable = false, unique = true, length = 32) private String code;
    @Column(nullable = false, length = 120) private String title;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(nullable = false) private Airport destination;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private PackageType type;
    @Column(nullable = false, length = 600) private String description;
    @Column(nullable = false, length = 400) private String inclusions;
    @Column(nullable = false, length = 120) private String hotel;
    @Column(length = 260) private String imagePath;
    @Column(nullable = false) private int nights;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal pricePerPerson;
    private boolean active = true;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Airport getDestination() { return destination; }
    public void setDestination(Airport destination) { this.destination = destination; }
    public PackageType getType() { return type; }
    public void setType(PackageType type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getInclusions() { return inclusions; }
    public void setInclusions(String inclusions) { this.inclusions = inclusions; }
    public String getHotel() { return hotel; }
    public void setHotel(String hotel) { this.hotel = hotel; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public int getNights() { return nights; }
    public void setNights(int nights) { this.nights = nights; }
    public BigDecimal getPricePerPerson() { return pricePerPerson; }
    public void setPricePerPerson(BigDecimal pricePerPerson) { this.pricePerPerson = pricePerPerson; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
