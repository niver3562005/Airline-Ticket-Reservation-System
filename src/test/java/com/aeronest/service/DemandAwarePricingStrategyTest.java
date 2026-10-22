package com.aeronest.service;

import com.aeronest.domain.Aircraft;
import com.aeronest.domain.Flight;
import com.aeronest.domain.DomainEnums.CabinClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class DemandAwarePricingStrategyTest {
    private DemandAwarePricingStrategy strategy;
    private Flight flight;

    @BeforeEach
    void setUp() {
        strategy = new DemandAwarePricingStrategy();
        Aircraft aircraft = new Aircraft(); aircraft.setBusinessRows(2); aircraft.setEconomyRows(8); aircraft.setSeatsPerRow(5);
        flight = new Flight(); flight.setAircraft(aircraft); flight.setEconomyFare(new BigDecimal("10000")); flight.setBusinessFare(new BigDecimal("25000"));
    }

    @Test void returnsBaseFareWhenLoadIsLow() {
        assertThat(strategy.calculate(flight, CabinClass.ECONOMY, 10, null)).isEqualByComparingTo("10000.00");
    }
    @Test void addsTenPercentAtSixtyPercentLoad() {
        assertThat(strategy.calculate(flight, CabinClass.ECONOMY, 30, null)).isEqualByComparingTo("11000.00");
    }
    @Test void addsTwentyPercentAtEightyPercentLoad() {
        assertThat(strategy.calculate(flight, CabinClass.ECONOMY, 40, null)).isEqualByComparingTo("12000.00");
    }
    @Test void usesBusinessBaseFare() {
        assertThat(strategy.calculate(flight, CabinClass.BUSINESS, 0, null)).isEqualByComparingTo("25000.00");
    }
    @Test void appliesFlyTenPromoAfterDemandAdjustment() {
        assertThat(strategy.calculate(flight, CabinClass.ECONOMY, 40, "fly10")).isEqualByComparingTo("10800.00");
    }
}
