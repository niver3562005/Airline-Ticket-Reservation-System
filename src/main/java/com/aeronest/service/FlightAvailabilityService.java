package com.aeronest.service;

import com.aeronest.domain.Flight;
import com.aeronest.domain.DomainEnums.CabinClass;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.math.BigDecimal;

@Service
public class FlightAvailabilityService {
    private final SeatMapService seats;
    public FlightAvailabilityService(SeatMapService seats) { this.seats = seats; }

    public List<CabinAvailability> forFlight(Flight flight) {
        return Arrays.stream(CabinClass.values()).map(cabin -> {
            long capacity = seats.seatMap(flight).stream().filter(value -> value.cabin() == cabin).count();
            int available = seats.availableSeats(flight, cabin).size();
            return new CabinAvailability(cabin, (int) capacity, available, (int) capacity - available, flight.getFareFor(cabin));
        }).toList();
    }
    public int available(Flight flight, CabinClass cabin) { return seats.availableSeats(flight, cabin).size(); }

    public record CabinAvailability(CabinClass cabin, int capacity, int available, int booked, BigDecimal fare) {
        public int getLoadPercent() { return capacity == 0 ? 0 : (int) Math.round((booked * 100.0) / capacity); }
        public String getDisplayName() { return cabin.name().replace('_', ' '); }
    }
}
