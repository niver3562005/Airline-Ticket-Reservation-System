package com.aeronest.repository;

import com.aeronest.domain.Aircraft;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AircraftRepository extends JpaRepository<Aircraft, Long> {
    List<Aircraft> findAllByOrderByRegistrationAsc();
    List<Aircraft> findByActiveTrueOrderByRegistrationAsc();
    Optional<Aircraft> findByRegistrationIgnoreCase(String registration);
}
