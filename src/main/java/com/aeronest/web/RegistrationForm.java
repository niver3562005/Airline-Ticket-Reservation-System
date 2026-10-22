package com.aeronest.web;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationForm {
    @NotBlank @Size(max = 60) private String firstName;
    @NotBlank @Size(max = 60) private String lastName;
    @NotBlank @Email @Size(max = 160) private String email;
    @NotBlank @Pattern(regexp = "^[+0-9][0-9 -]{7,16}$", message = "Enter a valid phone number") private String phone;
    @NotBlank @Size(min = 8, max = 72) @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "Use upper and lowercase letters and a number") private String password;
    @NotBlank private String confirmPassword;
    private boolean termsAccepted;
    private boolean marketingOptIn;

    @AssertTrue(message = "Passwords must match") public boolean isPasswordMatching() { return password != null && password.equals(confirmPassword); }
    @AssertTrue(message = "Accept the terms to create an account") public boolean isTermsAccepted() { return termsAccepted; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public void setTermsAccepted(boolean termsAccepted) { this.termsAccepted = termsAccepted; }
    public boolean isMarketingOptIn() { return marketingOptIn; }
    public void setMarketingOptIn(boolean marketingOptIn) { this.marketingOptIn = marketingOptIn; }
}
