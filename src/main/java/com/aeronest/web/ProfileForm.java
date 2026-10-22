package com.aeronest.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProfileForm {
    @NotBlank @Size(max = 60) private String firstName;
    @NotBlank @Size(max = 60) private String lastName;
    @NotBlank @Pattern(regexp = "^[+0-9][0-9 -]{7,16}$", message = "Enter a valid phone number") private String phone;
    private boolean marketingOptIn;
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public boolean isMarketingOptIn() { return marketingOptIn; }
    public void setMarketingOptIn(boolean marketingOptIn) { this.marketingOptIn = marketingOptIn; }
}
