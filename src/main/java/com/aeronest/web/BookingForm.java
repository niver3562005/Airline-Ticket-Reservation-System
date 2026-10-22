package com.aeronest.web;

import com.aeronest.domain.DomainEnums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class BookingForm {
    @NotNull private Long flightId;
    @NotBlank @Size(min = 3, max = 100) private String contactName;
    @NotBlank @Email private String contactEmail;
    @NotBlank @Pattern(regexp = "^[+0-9][0-9 -]{7,16}$", message = "Enter a valid phone number")
    private String contactPhone;
    @Size(max = 20) private String promoCode;
    @NotNull private PaymentMethod paymentMethod = PaymentMethod.CARD;
    @Valid @NotEmpty @Size(max = 6) private List<PassengerForm> passengers = new ArrayList<>();

    public Long getFlightId() { return flightId; }
    public void setFlightId(Long flightId) { this.flightId = flightId; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public List<PassengerForm> getPassengers() { return passengers; }
    public void setPassengers(List<PassengerForm> passengers) { this.passengers = passengers; }
}
