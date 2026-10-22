package com.aeronest.domain;

import com.aeronest.domain.DomainEnums.PartnerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "travel_partners")
public class TravelPartner extends BaseEntity {
    @Column(nullable = false, unique = true, length = 24) private String code;
    @Column(nullable = false, length = 120) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private PartnerType type;
    @ManyToOne(fetch = FetchType.EAGER) private Airport airport;
    @Column(nullable = false, length = 80) private String city;
    @Column(nullable = false, length = 80) private String country;
    @Column(nullable = false, length = 700) private String description;
    @Column(length = 180) private String serviceHighlights;
    @Column(length = 40) private String contactPhone;
    @Column(length = 160) private String contactEmail;
    @Column(length = 260) private String website;
    @Column(length = 260) private String imagePath;
    @Column(precision = 12, scale = 2) private BigDecimal priceFrom;
    @Column(nullable = false, length = 3) private String currency = "LKR";
    @Column(precision = 2, scale = 1) private BigDecimal rating = new BigDecimal("4.5");
    private boolean verified = true;
    private boolean open24Hours;
    private boolean active = true;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public PartnerType getType() { return type; }
    public void setType(PartnerType type) { this.type = type; }
    public Airport getAirport() { return airport; }
    public void setAirport(Airport airport) { this.airport = airport; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getServiceHighlights() { return serviceHighlights; }
    public void setServiceHighlights(String serviceHighlights) { this.serviceHighlights = serviceHighlights; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public BigDecimal getPriceFrom() { return priceFrom; }
    public void setPriceFrom(BigDecimal priceFrom) { this.priceFrom = priceFrom; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public boolean isOpen24Hours() { return open24Hours; }
    public void setOpen24Hours(boolean open24Hours) { this.open24Hours = open24Hours; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
