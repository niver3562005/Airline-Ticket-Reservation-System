package com.aeronest.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PromotionCatalogService {
    public record Promotion(String code, String title, String description, int discountPercent,
                            String audience, LocalDate validUntil, String visualClass) {}

    private static final List<Promotion> OFFERS = List.of(
            new Promotion("FLY10", "Take 10% off your flight", "A flexible saving across all four cabins on demonstration fares.", 10, "All travellers", LocalDate.of(2027, 12, 31), "promo-flight"),
            new Promotion("WELCOME20", "A warmer welcome", "Save 20% on the first AeroVista booking created for this educational platform.", 20, "New members", LocalDate.of(2027, 12, 31), "promo-welcome"),
            new Promotion("FAMILY12", "Family journeys made lighter", "Save 12% when arranging a family journey and connected airport services.", 12, "Family travel", LocalDate.of(2027, 8, 31), "promo-family"),
            new Promotion("BIZ15", "Arrive business-ready", "Save 15% on Business and First demonstration fares with priority journey support.", 15, "Premium cabins", LocalDate.of(2027, 6, 30), "promo-business")
    );

    public List<Promotion> activeOffers() {
        return OFFERS.stream().filter(offer -> !offer.validUntil().isBefore(LocalDate.now())).toList();
    }

    public Optional<Promotion> find(String code) {
        if (code == null || code.isBlank()) return Optional.empty();
        return activeOffers().stream().filter(offer -> offer.code().equalsIgnoreCase(code.trim())).findFirst();
    }

    public static BigDecimal multiplier(String code) {
        if (code == null || code.isBlank()) return BigDecimal.ONE;
        return OFFERS.stream().filter(offer -> offer.code().equalsIgnoreCase(code.trim()))
                .filter(offer -> !offer.validUntil().isBefore(LocalDate.now()))
                .findFirst().map(offer -> BigDecimal.ONE.subtract(BigDecimal.valueOf(offer.discountPercent(), 2)))
                .orElse(BigDecimal.ONE);
    }
}
