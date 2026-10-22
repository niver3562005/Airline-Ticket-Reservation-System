package com.aeronest.service;

import com.aeronest.domain.Flight;
import com.aeronest.domain.DomainEnums.CabinClass;
import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculate(Flight flight, CabinClass cabin, long occupiedSeats, String promoCode);
}
