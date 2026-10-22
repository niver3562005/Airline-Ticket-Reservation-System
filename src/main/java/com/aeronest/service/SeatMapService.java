package com.aeronest.service;

import com.aeronest.domain.Aircraft;
import com.aeronest.domain.Flight;
import com.aeronest.domain.Passenger;
import com.aeronest.domain.DomainEnums.BookingStatus;
import com.aeronest.domain.DomainEnums.CabinClass;
import com.aeronest.repository.PassengerRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SeatMapService {
    private final PassengerRepository passengers;
    public SeatMapService(PassengerRepository passengers) { this.passengers = passengers; }

    public List<SeatView> seatMap(Flight flight) {
        Set<String> occupied = new HashSet<>(passengers.findByFlightIdAndBookingStatusNot(flight.getId(), BookingStatus.CANCELLED)
                .stream().map(Passenger::getSeatNumber).toList());
        Aircraft a = flight.getAircraft();
        List<SeatView> result = new ArrayList<>();
        int totalRows = a.getFirstRows() + a.getBusinessRows() + a.getPremiumEconomyRows() + a.getEconomyRows();
        for (int row = 1; row <= totalRows; row++) {
            CabinClass cabin = cabinAtRow(a, row);
            for (int position = 0; position < a.getSeatsPerRow(); position++) {
                String label = row + String.valueOf((char) ('A' + position));
                result.add(new SeatView(label, cabin, occupied.contains(label), position == 0 || position == a.getSeatsPerRow() - 1));
            }
        }
        return result;
    }
    public List<String> availableSeats(Flight flight, CabinClass cabin) {
        return seatMap(flight).stream().filter(s -> s.cabin() == cabin && !s.occupied()).map(SeatView::label).toList();
    }
    public CabinClass cabinFor(Flight flight, String seat) {
        int row;
        try { row = Integer.parseInt(seat.replaceAll("[^0-9]", "")); }
        catch (RuntimeException ex) { throw new BusinessRuleException("Invalid seat number"); }
        Aircraft aircraft = flight.getAircraft();
        int max = aircraft.getFirstRows() + aircraft.getBusinessRows() + aircraft.getPremiumEconomyRows() + aircraft.getEconomyRows();
        if (row < 1 || row > max) throw new BusinessRuleException("Seat is outside the aircraft layout");
        return cabinAtRow(aircraft, row);
    }
    private CabinClass cabinAtRow(Aircraft aircraft, int row) {
        if (row <= aircraft.getFirstRows()) return CabinClass.FIRST;
        int businessEnd = aircraft.getFirstRows() + aircraft.getBusinessRows();
        if (row <= businessEnd) return CabinClass.BUSINESS;
        int premiumEnd = businessEnd + aircraft.getPremiumEconomyRows();
        if (row <= premiumEnd) return CabinClass.PREMIUM_ECONOMY;
        return CabinClass.ECONOMY;
    }
    public record SeatView(String label, CabinClass cabin, boolean occupied, boolean window) {}
}
