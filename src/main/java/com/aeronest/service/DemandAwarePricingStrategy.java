package com.aeronest.service;

import com.aeronest.domain.Flight;
import com.aeronest.domain.DomainEnums.CabinClass;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DemandAwarePricingStrategy implements PricingStrategy {
    @Override
    public BigDecimal calculate(Flight flight, CabinClass cabin, long occupiedSeats, String promoCode) {
        BigDecimal base = flight.getFareFor(cabin);
        double load = flight.getAircraft().getCapacity() == 0 ? 0 : (double) occupiedSeats / flight.getAircraft().getCapacity();
        BigDecimal multiplier = load >= .8 ? new BigDecimal("1.20") : load >= .6 ? new BigDecimal("1.10") : BigDecimal.ONE;
        BigDecimal price = base.multiply(multiplier);
        price = price.multiply(PromotionCatalogService.multiplier(promoCode));
        return price.setScale(2, RoundingMode.HALF_UP);
    }
}
