package com.aeronest.domain;

import com.aeronest.domain.DomainEnums.LoyaltyTier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_accounts")
public class CustomerAccount extends BaseEntity {
    @Column(nullable = false, length = 60)
    private String firstName;
    @Column(nullable = false, length = 60)
    private String lastName;
    @Column(nullable = false, unique = true, length = 160)
    private String email;
    @Column(nullable = false, length = 100)
    private String passwordHash;
    @Column(length = 24)
    private String phone;
    @Column(nullable = false, unique = true, length = 16)
    private String loyaltyNumber;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private LoyaltyTier loyaltyTier = LoyaltyTier.EXPLORER;
    @Column(nullable = false)
    private int loyaltyPoints;
    @Column(nullable = false)
    private boolean emailVerified;
    @Column(nullable = false)
    private boolean marketingOptIn;

    public String getFullName() { return firstName + " " + lastName; }
    public String getInitials() { return (firstName.substring(0, 1) + lastName.substring(0, 1)).toUpperCase(); }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email == null ? null : email.trim().toLowerCase(); }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLoyaltyNumber() { return loyaltyNumber; }
    public void setLoyaltyNumber(String loyaltyNumber) { this.loyaltyNumber = loyaltyNumber; }
    public LoyaltyTier getLoyaltyTier() { return loyaltyTier; }
    public void setLoyaltyTier(LoyaltyTier loyaltyTier) { this.loyaltyTier = loyaltyTier; }
    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public boolean isMarketingOptIn() { return marketingOptIn; }
    public void setMarketingOptIn(boolean marketingOptIn) { this.marketingOptIn = marketingOptIn; }
}
