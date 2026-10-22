package com.aeronest.web;

import com.aeronest.domain.DomainEnums.CabinClass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class PassengerForm {
    @NotBlank @Size(max = 60) private String firstName;
    @NotBlank @Size(max = 60) private String lastName;
    @NotNull @Past private LocalDate dateOfBirth;
    @NotBlank @Size(max = 60) private String nationality;
    @NotBlank @Pattern(regexp = "^[A-Za-z0-9]{5,15}$", message = "Use 5-15 letters or numbers")
    private String passportNumber;
    @NotBlank @Pattern(regexp = "^[0-9]{1,2}[A-J]$", message = "Choose a valid seat")
    private String seatNumber;
    @NotNull private CabinClass cabinClass;
    @Size(max = 80) private String mealPreference;
    @Size(max = 240) private String specialAssistance;
    @Pattern(regexp = "^$|^[A-Za-z0-9-]{4,30}$", message = "Use 4-30 letters, numbers or hyphens")
    private String frequentFlyerNumber;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public String getPassportNumber() { return passportNumber; }
    public void setPassportNumber(String passportNumber) { this.passportNumber = passportNumber; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public CabinClass getCabinClass() { return cabinClass; }
    public void setCabinClass(CabinClass cabinClass) { this.cabinClass = cabinClass; }
    public String getMealPreference() { return mealPreference; }
    public void setMealPreference(String mealPreference) { this.mealPreference = mealPreference; }
    public String getSpecialAssistance() { return specialAssistance; }
    public void setSpecialAssistance(String specialAssistance) { this.specialAssistance = specialAssistance; }
    public String getFrequentFlyerNumber() { return frequentFlyerNumber; }
    public void setFrequentFlyerNumber(String frequentFlyerNumber) { this.frequentFlyerNumber = frequentFlyerNumber; }
}
