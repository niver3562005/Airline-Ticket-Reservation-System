package com.aeronest.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

public class PartnerReservationForm {
    @NotNull private Long partnerId;
    @NotBlank @Size(min = 3, max = 120) private String travellerName;
    @NotBlank @Email @Size(max = 160) private String email;
    @NotBlank @Pattern(regexp = "^[+0-9][0-9 -]{7,16}$", message = "Enter a valid contact number") private String phone;
    @NotNull @Future @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) private LocalDateTime serviceAt;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) private LocalDateTime serviceEndsAt;
    @Min(1) @Max(12) private int guests = 1;
    @Size(max = 180) private String pickupLocation;
    @Size(max = 180) private String dropoffLocation;
    @Size(max = 500) private String notes;
    @Size(max = 8) private String airlinePnr;

    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }
    public String getTravellerName() { return travellerName; }
    public void setTravellerName(String travellerName) { this.travellerName = travellerName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDateTime getServiceAt() { return serviceAt; }
    public void setServiceAt(LocalDateTime serviceAt) { this.serviceAt = serviceAt; }
    public LocalDateTime getServiceEndsAt() { return serviceEndsAt; }
    public void setServiceEndsAt(LocalDateTime serviceEndsAt) { this.serviceEndsAt = serviceEndsAt; }
    public int getGuests() { return guests; }
    public void setGuests(int guests) { this.guests = guests; }
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    public String getDropoffLocation() { return dropoffLocation; }
    public void setDropoffLocation(String dropoffLocation) { this.dropoffLocation = dropoffLocation; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getAirlinePnr() { return airlinePnr; }
    public void setAirlinePnr(String airlinePnr) { this.airlinePnr = airlinePnr; }
}
