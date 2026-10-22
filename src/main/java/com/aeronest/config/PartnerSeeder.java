package com.aeronest.config;

import com.aeronest.domain.Airport;
import com.aeronest.domain.TravelPartner;
import com.aeronest.domain.DomainEnums.PartnerType;
import com.aeronest.repository.AirportRepository;
import com.aeronest.repository.TravelPartnerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(3)
public class PartnerSeeder implements CommandLineRunner {
    private final TravelPartnerRepository partners;
    private final AirportRepository airports;

    public PartnerSeeder(TravelPartnerRepository partners, AirportRepository airports) {
        this.partners = partners; this.airports = airports;
    }

    @Override public void run(String... args) {
        if (airports.count() == 0) return;
        List<TravelPartner> catalogue = new ArrayList<>(List.of(
                partner("AG-CMB-01", "Ceylon Journey Desk", PartnerType.TRAVEL_AGENCY, "CMB", "Colombo", "Sri Lanka",
                        "A verified agency desk for tailor-made itineraries, group travel and destination support.", "Multi-city planning · group fares · visa-document checklist", 3500, "4.8", true),
                partner("AG-DXB-01", "Gulf Connect Travel", PartnerType.TRAVEL_AGENCY, "DXB", "Dubai", "United Arab Emirates",
                        "Local connection assistance for stopovers, excursions and onward regional travel.", "Stopover desk · attraction passes · Arabic and English support", 4200, "4.7", true),
                partner("HT-DXB-01", "Marina Horizon Hotel", PartnerType.HOTEL, "DXB", "Dubai", "United Arab Emirates",
                        "Contemporary airport-connected stay with flexible arrival support for disrupted journeys.", "Breakfast · airport shuttle · late check-in", 38500, "4.7", true),
                partner("HT-SIN-01", "Changi Garden Suites", PartnerType.HOTEL, "SIN", "Singapore", "Singapore",
                        "Quiet transit-friendly rooms with rapid airport transfers and workspace facilities.", "Breakfast · transfer · workspace · family rooms", 46200, "4.8", true),
                partner("HT-MLE-01", "Lagoon Arrival Hotel", PartnerType.HOTEL, "MLE", "Malé", "Maldives",
                        "A convenient first-night stay with resort-transfer coordination for island arrivals.", "Speedboat desk · breakfast · luggage storage", 41900, "4.6", true),
                partner("CB-CMB-01", "AeroCab Colombo", PartnerType.CAB, "CMB", "Colombo", "Sri Lanka",
                        "Pre-booked airport transport with flight-aware pickup timing and fixed pricing.", "Meet and greet · 60-minute wait · live driver contact", 6500, "4.9", true),
                partner("CB-DXB-01", "Skyline Airport Cars", PartnerType.CAB, "DXB", "Dubai", "United Arab Emirates",
                        "Licensed airport transfer service with sedans, family vehicles and executive cars.", "Flight monitoring · child seat · card payment", 12800, "4.7", true),
                partner("CB-SIN-01", "Lion City Transfer", PartnerType.CAB, "SIN", "Singapore", "Singapore",
                        "Reliable terminal pickup with transparent destination-zone prices.", "Arrival paging · wheelchair vehicle on request", 10400, "4.8", true),
                partner("CF-CMB-01", "Runway Roast", PartnerType.CAFE, "CMB", "Colombo", "Sri Lanka",
                        "Pre-order refreshments for collection before security or at the departure gate.", "Vegetarian options · takeaway · pre-order", 1800, "4.5", false),
                partner("CF-SIN-01", "Orchid Terminal Kitchen", PartnerType.CAFE, "SIN", "Singapore", "Singapore",
                        "All-day dining for short connections, with allergy-aware meal choices.", "Halal choices · allergen labels · terminal delivery", 2900, "4.7", true),
                partner("SM-GLOBAL-01", "AeroSIM Global", PartnerType.SIM, null, "Worldwide", "Global",
                        "Travel eSIM data plans activated before departure with instant QR delivery.", "120+ countries · instant activation · hotspot enabled", 2400, "4.8", true),
                partner("SM-DXB-01", "GulfLink SIM", PartnerType.SIM, "DXB", "Dubai", "United Arab Emirates",
                        "Physical and eSIM connectivity packages for UAE arrivals.", "Airport collection · 10 GB data · local number", 3900, "4.6", true)
        ));
        catalogue.addAll(List.of(
                partner("CB-CMB-SH", "AeroVista Terminal Shuttle", PartnerType.CAB, "CMB", "Colombo", "Sri Lanka", "Shared terminal-to-city shuttle with flight-aware dispatch and protected connection windows.", "Shared shuttle · flight tracking · 24/7 dispatch", 3200, "4.8", true),
                partner("CB-DXB-EX", "Dubai Executive Chauffeur", PartnerType.CAB, "DXB", "Dubai", "United Arab Emirates", "Premium chauffeur transfer for Business and First travellers with curbside meet-and-greet.", "Executive sedan · name board · 90-minute wait", 18500, "4.9", true),
                partner("CB-SIN-SH", "Changi City Shuttle", PartnerType.CAB, "SIN", "Singapore", "Singapore", "Scheduled airport shuttle connecting central hotels with every terminal.", "Fixed fare · luggage included · accessible vehicle", 7200, "4.8", true),
                partner("CB-MLE-BOAT", "Malé Marina Transfer", PartnerType.CAB, "MLE", "Malé", "Maldives", "Coordinated airport-to-marina transfer with resort speedboat handover.", "Arrival escort · luggage transfer · resort coordination", 9600, "4.7", true),
                partner("AG-GLOBAL-01", "AeroVista Complex Journeys", PartnerType.TRAVEL_AGENCY, null, "Worldwide", "Global", "Specialist support for multi-city, groups, stopovers and accessible itineraries.", "Complex fares · destination experts · disruption support", 5000, "4.9", true),
                partner("CF-DXB-01", "Golden Gate Dining", PartnerType.CAFE, "DXB", "Dubai", "United Arab Emirates", "Pre-book a table or collect a chef-prepared meal during your connection.", "Pre-order · dietary filters · lounge delivery", 4200, "4.8", true)
        ));
        int index = 0;
        for (HotelSeed hotel : hotelCatalogue()) {
            int price = 24500 + (index++ % 10) * 3100;
            catalogue.add(partner("HT-" + hotel.code() + "-CITY", hotel.city() + " Grand Hotel", PartnerType.HOTEL,
                    null, hotel.city(), hotel.country(),
                    "A flight-aware city stay selected for dependable arrivals, flexible reception and convenient local access.",
                    "Breakfast · flexible check-in · airport transfer option · Wi-Fi", price, index % 3 == 0 ? "4.9" : "4.7", true));
        }
        catalogue.forEach(this::upsert);
    }

    private void upsert(TravelPartner candidate) {
        TravelPartner value = partners.findByCodeIgnoreCase(candidate.getCode()).orElse(candidate);
        value.setImagePath(candidate.getImagePath());
        value.setActive(true);
        partners.save(value);
    }

    private TravelPartner partner(String code, String name, PartnerType type, String airportCode, String city, String country,
                                  String description, String highlights, int price, String rating, boolean open24Hours) {
        TravelPartner value = new TravelPartner(); value.setCode(code); value.setName(name); value.setType(type);
        if (airportCode != null) value.setAirport(airport(airportCode));
        value.setCity(city); value.setCountry(country); value.setDescription(description); value.setServiceHighlights(highlights);
        value.setContactPhone("+94 11 555 2026"); value.setContactEmail("partners@aerovista.test");
        value.setPriceFrom(BigDecimal.valueOf(price)); value.setRating(new BigDecimal(rating)); value.setOpen24Hours(open24Hours);
        value.setImagePath(imageFor(type)); return value;
    }

    private String imageFor(PartnerType type) {
        return switch (type) {
            case HOTEL -> "/images/aerovista-hotels-v1.png";
            case CAB -> "/images/aerovista-transfers-v1.png";
            case TRAVEL_AGENCY -> "/images/aerovista-agencies-v1.png";
            case CAFE -> "/images/aerovista-dining-v1.png";
            case SIM -> "/images/aerovista-connect-v1.png";
        };
    }

    private List<HotelSeed> hotelCatalogue() {
        return List.of(
                new HotelSeed("AU", "Sydney", "Australia"), new HotelSeed("AT", "Vienna", "Austria"),
                new HotelSeed("BH", "Manama", "Bahrain"), new HotelSeed("BD", "Dhaka", "Bangladesh"),
                new HotelSeed("BE", "Brussels", "Belgium"), new HotelSeed("BR", "Rio de Janeiro", "Brazil"),
                new HotelSeed("KH", "Phnom Penh", "Cambodia"), new HotelSeed("CA", "Toronto", "Canada"),
                new HotelSeed("CN", "Beijing", "China"), new HotelSeed("DK", "Copenhagen", "Denmark"),
                new HotelSeed("EG", "Cairo", "Egypt"), new HotelSeed("FI", "Helsinki", "Finland"),
                new HotelSeed("FR", "Paris", "France"), new HotelSeed("DE", "Frankfurt", "Germany"),
                new HotelSeed("GR", "Athens", "Greece"), new HotelSeed("HK", "Hong Kong", "Hong Kong"),
                new HotelSeed("IN", "New Delhi", "India"), new HotelSeed("ID", "Bali", "Indonesia"),
                new HotelSeed("IE", "Dublin", "Ireland"), new HotelSeed("IT", "Rome", "Italy"),
                new HotelSeed("JP", "Tokyo", "Japan"), new HotelSeed("JO", "Amman", "Jordan"),
                new HotelSeed("KE", "Nairobi", "Kenya"), new HotelSeed("KW", "Kuwait City", "Kuwait"),
                new HotelSeed("MY", "Kuala Lumpur", "Malaysia"), new HotelSeed("MV", "Malé", "Maldives"),
                new HotelSeed("MU", "Port Louis", "Mauritius"), new HotelSeed("NP", "Kathmandu", "Nepal"),
                new HotelSeed("NL", "Amsterdam", "Netherlands"), new HotelSeed("NZ", "Auckland", "New Zealand"),
                new HotelSeed("NO", "Oslo", "Norway"), new HotelSeed("OM", "Muscat", "Oman"),
                new HotelSeed("PK", "Islamabad", "Pakistan"), new HotelSeed("PH", "Manila", "Philippines"),
                new HotelSeed("PT", "Lisbon", "Portugal"), new HotelSeed("QA", "Doha", "Qatar"),
                new HotelSeed("SA", "Riyadh", "Saudi Arabia"), new HotelSeed("SG", "Singapore", "Singapore"),
                new HotelSeed("ZA", "Cape Town", "South Africa"), new HotelSeed("KR", "Seoul", "South Korea"),
                new HotelSeed("ES", "Barcelona", "Spain"), new HotelSeed("LK", "Colombo", "Sri Lanka"),
                new HotelSeed("SE", "Stockholm", "Sweden"), new HotelSeed("CH", "Zurich", "Switzerland"),
                new HotelSeed("TH", "Bangkok", "Thailand"), new HotelSeed("TR", "Istanbul", "Turkey"),
                new HotelSeed("AE", "Dubai", "United Arab Emirates"), new HotelSeed("GB", "London", "United Kingdom"),
                new HotelSeed("US", "New York", "United States"), new HotelSeed("VN", "Hanoi", "Vietnam")
        );
    }

    private record HotelSeed(String code, String city, String country) {}

    private Airport airport(String code) { return airports.findByIataCodeIgnoreCase(code).orElseThrow(); }
}
