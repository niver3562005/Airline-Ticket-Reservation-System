package com.aeronest.service;

import com.aeronest.domain.Airport;
import com.aeronest.repository.AirportRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AirportService {
    private final AirportRepository airports;
    public AirportService(AirportRepository airports) { this.airports = airports; }
    public List<Airport> all() { return airports.findAllByOrderByCityAsc(); }
    public List<Airport> active() { return airports.findByActiveTrueOrderByCityAsc(); }
    public Airport get(Long id) { return airports.findById(id).orElseThrow(() -> new ResourceNotFoundException("Airport not found")); }
    @Transactional public Airport save(Airport airport) {
        airports.findByIataCodeIgnoreCase(airport.getIataCode()).filter(a -> !a.getId().equals(airport.getId()))
                .ifPresent(a -> { throw new BusinessRuleException("That IATA code already exists"); });
        return airports.save(airport);
    }
    @Transactional public void delete(Long id) {
        try { airports.delete(get(id)); airports.flush(); }
        catch (DataIntegrityViolationException ex) { throw new BusinessRuleException("This airport is used by scheduled flights. Deactivate it instead."); }
    }
}
