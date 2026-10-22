package com.aeronest.repository;

import com.aeronest.domain.TravelPartner;
import com.aeronest.domain.DomainEnums.PartnerType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TravelPartnerRepository extends JpaRepository<TravelPartner, Long> {
    List<TravelPartner> findByActiveTrueOrderByTypeAscRatingDesc();
    List<TravelPartner> findByActiveTrueAndTypeOrderByRatingDesc(PartnerType type);
    Optional<TravelPartner> findByCodeIgnoreCase(String code);
}
