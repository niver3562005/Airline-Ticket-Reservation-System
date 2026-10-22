package com.aeronest.web;

import com.aeronest.domain.DomainEnums.CabinClass;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class FlightSearchForm {
    @NotNull private TripType tripType = TripType.ROUND_TRIP;
    @NotNull private Long originId;
    @NotNull private Long destinationId;
    @NotNull @FutureOrPresent @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate departureDate = LocalDate.now().plusDays(1);
    @FutureOrPresent @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate returnDate;
    @Min(1) @Max(6) private int adults = 1;
    @Min(0) @Max(5) private int children;
    @Min(0) @Max(2) private int infants;
    @NotNull private CabinClass cabinClass = CabinClass.ECONOMY;
    public enum TripType { ONE_WAY, ROUND_TRIP, MULTI_CITY }
    @AssertTrue(message = "Choose a return date after departure")
    public boolean isReturnDateValid() { return tripType != TripType.ROUND_TRIP || (returnDate != null && !returnDate.isBefore(departureDate)); }
    @AssertTrue(message = "A booking supports up to 6 travellers")
    public boolean isTravellerCountValid() { return getTravellers() >= 1 && getTravellers() <= 6 && infants <= adults; }
    public TripType getTripType() { return tripType; }
    public void setTripType(TripType tripType) { this.tripType = tripType; }
    public Long getOriginId() { return originId; }
    public void setOriginId(Long originId) { this.originId = originId; }
    public Long getDestinationId() { return destinationId; }
    public void setDestinationId(Long destinationId) { this.destinationId = destinationId; }
    public LocalDate getDepartureDate() { return departureDate; }
    public void setDepartureDate(LocalDate departureDate) { this.departureDate = departureDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public int getAdults() { return adults; }
    public void setAdults(int adults) { this.adults = adults; }
    public int getChildren() { return children; }
    public void setChildren(int children) { this.children = children; }
    public int getInfants() { return infants; }
    public void setInfants(int infants) { this.infants = infants; }
    public CabinClass getCabinClass() { return cabinClass; }
    public void setCabinClass(CabinClass cabinClass) { this.cabinClass = cabinClass; }
    public int getTravellers() { return adults + children + infants; }
    public void setTravellers(int travellers) { this.adults = travellers; this.children = 0; this.infants = 0; }
    public String getTravellerSummary() {
        return getTravellers() + (getTravellers() == 1 ? " traveller" : " travellers") + " · " +
                cabinClass.name().substring(0, 1) + cabinClass.name().substring(1).toLowerCase();
    }
}
