package com.aeronest.service;

import com.aeronest.domain.Aircraft;
import com.aeronest.repository.AircraftRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AircraftService {
    private final AircraftRepository aircraft;
    public AircraftService(AircraftRepository aircraft) { this.aircraft = aircraft; }
    public List<Aircraft> all() { return aircraft.findAllByOrderByRegistrationAsc(); }
    public List<Aircraft> active() { return aircraft.findByActiveTrueOrderByRegistrationAsc(); }
    public Aircraft get(Long id) { return aircraft.findById(id).orElseThrow(() -> new ResourceNotFoundException("Aircraft not found")); }
    @Transactional public Aircraft save(Aircraft value) {
        aircraft.findByRegistrationIgnoreCase(value.getRegistration()).filter(a -> !a.getId().equals(value.getId()))
                .ifPresent(a -> { throw new BusinessRuleException("That aircraft registration already exists"); });
        return aircraft.save(value);
    }
    @Transactional public void delete(Long id) {
        try { aircraft.delete(get(id)); aircraft.flush(); }
        catch (DataIntegrityViolationException ex) { throw new BusinessRuleException("This aircraft has scheduled flights. Deactivate it instead."); }
    }
}
