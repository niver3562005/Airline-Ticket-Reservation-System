package com.aeronest.repository;

import com.aeronest.domain.TravelPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TravelPackageRepository extends JpaRepository<TravelPackage, Long> {
    List<TravelPackage> findByActiveTrueOrderByPricePerPersonAsc();
    boolean existsByCodeIgnoreCase(String code);
}
