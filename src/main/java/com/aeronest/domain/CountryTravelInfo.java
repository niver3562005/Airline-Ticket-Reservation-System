package com.aeronest.domain;

import com.aeronest.domain.DomainEnums.VisaRequirement;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "country_travel_info")
public class CountryTravelInfo extends BaseEntity {
    @Column(nullable = false, unique = true, length = 80) private String country;
    @Column(nullable = false, unique = true, length = 2) private String countryCode;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private VisaRequirement visaRequirement;
    @Column(nullable = false, length = 600) private String summary;
    @Column(nullable = false, length = 400) private String entryRequirements;
    @Column(nullable = false) private int passportValidityMonths;
    @Column(length = 120) private String arrivalForm;
    @Column(length = 260) private String officialUrl;
    @Column(length = 40) private String localCurrency;
    @Column(length = 40) private String emergencyNumber;
    @Column(length = 260) private String imagePath;
    @Column(length = 80) private String featuredCity;

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public VisaRequirement getVisaRequirement() { return visaRequirement; }
    public void setVisaRequirement(VisaRequirement visaRequirement) { this.visaRequirement = visaRequirement; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getEntryRequirements() { return entryRequirements; }
    public void setEntryRequirements(String entryRequirements) { this.entryRequirements = entryRequirements; }
    public int getPassportValidityMonths() { return passportValidityMonths; }
    public void setPassportValidityMonths(int passportValidityMonths) { this.passportValidityMonths = passportValidityMonths; }
    public String getArrivalForm() { return arrivalForm; }
    public void setArrivalForm(String arrivalForm) { this.arrivalForm = arrivalForm; }
    public String getOfficialUrl() { return officialUrl; }
    public void setOfficialUrl(String officialUrl) { this.officialUrl = officialUrl; }
    public String getLocalCurrency() { return localCurrency; }
    public void setLocalCurrency(String localCurrency) { this.localCurrency = localCurrency; }
    public String getEmergencyNumber() { return emergencyNumber; }
    public void setEmergencyNumber(String emergencyNumber) { this.emergencyNumber = emergencyNumber; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getFeaturedCity() { return featuredCity; }
    public void setFeaturedCity(String featuredCity) { this.featuredCity = featuredCity; }
}
