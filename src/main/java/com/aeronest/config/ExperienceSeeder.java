package com.aeronest.config;

import com.aeronest.domain.Aircraft;
import com.aeronest.domain.Airport;
import com.aeronest.domain.AirportFacility;
import com.aeronest.domain.CountryTravelInfo;
import com.aeronest.domain.Flight;
import com.aeronest.domain.TravelPackage;
import com.aeronest.domain.DomainEnums.FacilityType;
import com.aeronest.domain.DomainEnums.FlightStatus;
import com.aeronest.domain.DomainEnums.PackageType;
import com.aeronest.domain.DomainEnums.VisaRequirement;
import com.aeronest.repository.AircraftRepository;
import com.aeronest.repository.AirportFacilityRepository;
import com.aeronest.repository.AirportRepository;
import com.aeronest.repository.CountryTravelInfoRepository;
import com.aeronest.repository.FlightRepository;
import com.aeronest.repository.TravelPackageRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Value;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@Order(2)
public class ExperienceSeeder implements CommandLineRunner {
    private final CountryTravelInfoRepository countries;
    private final AirportFacilityRepository facilities;
    private final TravelPackageRepository packages;
    private final AirportRepository airports;
    private final AircraftRepository aircraft;
    private final FlightRepository flights;
    private final boolean seedLiveFlight;

    public ExperienceSeeder(CountryTravelInfoRepository countries, AirportFacilityRepository facilities,
                            TravelPackageRepository packages, AirportRepository airports,
                            AircraftRepository aircraft, FlightRepository flights,
                            @Value("${aeronest.demo.live-flight:true}") boolean seedLiveFlight) {
        this.countries = countries; this.facilities = facilities; this.packages = packages;
        this.airports = airports; this.aircraft = aircraft; this.flights = flights;
        this.seedLiveFlight = seedLiveFlight;
    }

    @Override public void run(String... args) {
        if (countries.count() == 0) seedCountries();
        expandCountryCatalogue();
        if (facilities.count() == 0 && airports.count() > 0) seedFacilities();
        if (packages.count() == 0 && airports.count() > 0) seedPackages();
        upgradePackageImagery();
        if (airports.count() > 0 && aircraft.count() > 0) seedConnections();
        upgradeOperationalData();
    }

    private void seedCountries() {
        countries.save(country("Singapore", "SG", VisaRequirement.VISA_REQUIRED,
                "Sri Lankan ordinary passport holders should obtain an entry visa before travel. A visa is pre-entry permission and final admission is decided at the checkpoint.",
                "Valid individual passport, onward or return travel, accommodation and sufficient funds.", 6, "SG Arrival Card within 3 days before arrival",
                "https://www.ica.gov.sg/enter-transit-depart/entering-singapore", "Singapore dollar (SGD)", "999"));
        countries.save(country("Maldives", "MV", VisaRequirement.VISA_ON_ARRIVAL,
                "Tourist visa is granted on arrival when immigration entry conditions are met; pre-approval is generally not required for tourists.",
                "Machine-readable passport, confirmed return or onward ticket, registered accommodation and sufficient funds.", 1, "IMUGA Traveller Declaration within 96 hours before arrival",
                "https://www.immigration.gov.mv/visa/tourist-visa", "Maldivian rufiyaa (MVR)", "119"));
        countries.save(country("India", "IN", VisaRequirement.EVISA_REQUIRED,
                "Eligible Sri Lankan travellers can apply through the official Indian e-Visa service. Apply before travel and carry the granted ETA.",
                "Passport with at least two blank pages, return or onward ticket and sufficient funds.", 6, "e-Arrival card within 72 hours before arrival",
                "https://indianvisaonline.gov.in/evisa/", "Indian rupee (INR)", "112"));
        countries.save(country("Thailand", "TH", VisaRequirement.CHECK_REQUIRED,
                "Visa eligibility depends on passport, document type and planned length of stay. Use the official Thai e-Visa checker before booking.",
                "Valid passport, confirmed accommodation, onward travel and evidence of travel funds may be requested.", 6, "Thailand Digital Arrival Card where applicable",
                "https://www.thaievisa.go.th/", "Thai baht (THB)", "191"));
        countries.save(country("United Arab Emirates", "AE", VisaRequirement.VISA_REQUIRED,
                "Entry permission depends on nationality, passport type and sponsor or airline arrangements. Confirm eligibility before departure.",
                "Valid passport, confirmed accommodation, return or onward travel and approved entry permission where required.", 6, "Check current federal entry requirements",
                "https://u.ae/en/information-and-services/visa-and-emirates-id", "UAE dirham (AED)", "999"));
        countries.save(country("Malaysia", "MY", VisaRequirement.CHECK_REQUIRED,
                "Check the official Malaysian immigration portal for the visa or electronic arrival requirements that apply to your passport.",
                "Valid passport, onward or return ticket, accommodation and sufficient funds.", 6, "Malaysia Digital Arrival Card where applicable",
                "https://www.imi.gov.my/", "Malaysian ringgit (MYR)", "999"));
        countries.save(country("Qatar", "QA", VisaRequirement.CHECK_REQUIRED,
                "Visa-free entry and visa-on-arrival eligibility vary by nationality and travel conditions. Verify before departure.",
                "Valid passport, confirmed accommodation, onward or return ticket and health insurance where required.", 6, "Check current Hayya and immigration requirements",
                "https://portal.moi.gov.qa/", "Qatari riyal (QAR)", "999"));
    }

    private CountryTravelInfo country(String name, String code, VisaRequirement visa, String summary, String requirements,
                                      int months, String form, String url, String currency, String emergency) {
        CountryTravelInfo info = new CountryTravelInfo(); info.setCountry(name); info.setCountryCode(code);
        info.setVisaRequirement(visa); info.setSummary(summary); info.setEntryRequirements(requirements);
        info.setPassportValidityMonths(months); info.setArrivalForm(form); info.setOfficialUrl(url);
        info.setLocalCurrency(currency); info.setEmergencyNumber(emergency); return info;
    }

    private void expandCountryCatalogue() {
        destinationCatalogue().forEach(seed -> {
            CountryTravelInfo info = countries.findByCountryCodeIgnoreCase(seed.code()).orElseGet(() -> country(
                    seed.country(), seed.code(), VisaRequirement.CHECK_REQUIRED,
                    "Entry and visa rules depend on nationality, passport type, trip purpose and length of stay. Check the latest requirements before purchase and again before departure.",
                    "Valid passport, confirmed accommodation, onward or return travel and proof of funds may be requested.",
                    6, "Check current digital arrival and health declarations",
                    "https://www.iatatravelcentre.com/", seed.currency(), seed.emergency()));
            info.setFeaturedCity(seed.city());
            info.setImagePath("/images/destinations/" + seed.code() + ".jpg");
            countries.save(info);
        });
    }

    private void upgradePackageImagery() {
        var availablePackages = packages.findAll();
        availablePackages.forEach(value -> value.setImagePath("/images/packages/" + value.getCode() + ".jpg"));
        packages.saveAll(availablePackages);
    }

    /** Fifty database-backed destinations used by the visa checker and visual destination gallery. */
    private List<DestinationSeed> destinationCatalogue() {
        return List.of(
                new DestinationSeed("Australia", "AU", "Sydney", "Australian dollar (AUD)", "000"),
                new DestinationSeed("Austria", "AT", "Vienna", "Euro (EUR)", "112"),
                new DestinationSeed("Bahrain", "BH", "Manama", "Bahraini dinar (BHD)", "999"),
                new DestinationSeed("Bangladesh", "BD", "Dhaka", "Bangladeshi taka (BDT)", "999"),
                new DestinationSeed("Belgium", "BE", "Brussels", "Euro (EUR)", "112"),
                new DestinationSeed("Brazil", "BR", "Rio de Janeiro", "Brazilian real (BRL)", "190"),
                new DestinationSeed("Cambodia", "KH", "Phnom Penh", "Cambodian riel (KHR)", "117"),
                new DestinationSeed("Canada", "CA", "Toronto", "Canadian dollar (CAD)", "911"),
                new DestinationSeed("China", "CN", "Beijing", "Chinese yuan (CNY)", "110"),
                new DestinationSeed("Denmark", "DK", "Copenhagen", "Danish krone (DKK)", "112"),
                new DestinationSeed("Egypt", "EG", "Cairo", "Egyptian pound (EGP)", "122"),
                new DestinationSeed("Finland", "FI", "Helsinki", "Euro (EUR)", "112"),
                new DestinationSeed("France", "FR", "Paris", "Euro (EUR)", "112"),
                new DestinationSeed("Germany", "DE", "Frankfurt", "Euro (EUR)", "112"),
                new DestinationSeed("Greece", "GR", "Athens", "Euro (EUR)", "112"),
                new DestinationSeed("Hong Kong", "HK", "Hong Kong", "Hong Kong dollar (HKD)", "999"),
                new DestinationSeed("India", "IN", "New Delhi", "Indian rupee (INR)", "112"),
                new DestinationSeed("Indonesia", "ID", "Bali", "Indonesian rupiah (IDR)", "112"),
                new DestinationSeed("Ireland", "IE", "Dublin", "Euro (EUR)", "112"),
                new DestinationSeed("Italy", "IT", "Rome", "Euro (EUR)", "112"),
                new DestinationSeed("Japan", "JP", "Tokyo", "Japanese yen (JPY)", "110"),
                new DestinationSeed("Jordan", "JO", "Amman", "Jordanian dinar (JOD)", "911"),
                new DestinationSeed("Kenya", "KE", "Nairobi", "Kenyan shilling (KES)", "999"),
                new DestinationSeed("Kuwait", "KW", "Kuwait City", "Kuwaiti dinar (KWD)", "112"),
                new DestinationSeed("Malaysia", "MY", "Kuala Lumpur", "Malaysian ringgit (MYR)", "999"),
                new DestinationSeed("Maldives", "MV", "Malé", "Maldivian rufiyaa (MVR)", "119"),
                new DestinationSeed("Mauritius", "MU", "Port Louis", "Mauritian rupee (MUR)", "999"),
                new DestinationSeed("Nepal", "NP", "Kathmandu", "Nepalese rupee (NPR)", "100"),
                new DestinationSeed("Netherlands", "NL", "Amsterdam", "Euro (EUR)", "112"),
                new DestinationSeed("New Zealand", "NZ", "Auckland", "New Zealand dollar (NZD)", "111"),
                new DestinationSeed("Norway", "NO", "Oslo", "Norwegian krone (NOK)", "112"),
                new DestinationSeed("Oman", "OM", "Muscat", "Omani rial (OMR)", "9999"),
                new DestinationSeed("Pakistan", "PK", "Islamabad", "Pakistani rupee (PKR)", "15"),
                new DestinationSeed("Philippines", "PH", "Manila", "Philippine peso (PHP)", "911"),
                new DestinationSeed("Portugal", "PT", "Lisbon", "Euro (EUR)", "112"),
                new DestinationSeed("Qatar", "QA", "Doha", "Qatari riyal (QAR)", "999"),
                new DestinationSeed("Saudi Arabia", "SA", "Riyadh", "Saudi riyal (SAR)", "911"),
                new DestinationSeed("Singapore", "SG", "Singapore", "Singapore dollar (SGD)", "999"),
                new DestinationSeed("South Africa", "ZA", "Cape Town", "South African rand (ZAR)", "112"),
                new DestinationSeed("South Korea", "KR", "Seoul", "South Korean won (KRW)", "112"),
                new DestinationSeed("Spain", "ES", "Barcelona", "Euro (EUR)", "112"),
                new DestinationSeed("Sri Lanka", "LK", "Colombo", "Sri Lankan rupee (LKR)", "119"),
                new DestinationSeed("Sweden", "SE", "Stockholm", "Swedish krona (SEK)", "112"),
                new DestinationSeed("Switzerland", "CH", "Zurich", "Swiss franc (CHF)", "112"),
                new DestinationSeed("Thailand", "TH", "Bangkok", "Thai baht (THB)", "191"),
                new DestinationSeed("Turkey", "TR", "Istanbul", "Turkish lira (TRY)", "112"),
                new DestinationSeed("United Arab Emirates", "AE", "Dubai", "UAE dirham (AED)", "999"),
                new DestinationSeed("United Kingdom", "GB", "London", "Pound sterling (GBP)", "999"),
                new DestinationSeed("United States", "US", "New York", "US dollar (USD)", "911"),
                new DestinationSeed("Vietnam", "VN", "Hanoi", "Vietnamese dong (VND)", "113")
        );
    }

    private record DestinationSeed(String country, String code, String city, String currency, String emergency) {}

    private void seedFacilities() {
        Airport cmb = airport("CMB"), dxb = airport("DXB"), sin = airport("SIN"), mle = airport("MLE");
        facilities.save(facility(cmb, FacilityType.LOUNGE, "Serendib Lounge", "Quiet work zones, dining and shower suites for eligible guests.", "Departures, Level 2", true, false));
        facilities.save(facility(cmb, FacilityType.WIFI, "Airport Wi-Fi", "Complimentary terminal-wide connectivity with charging points.", "All terminals", true, true));
        facilities.save(facility(cmb, FacilityType.PRAYER_ROOM, "Multi-faith rooms", "Dedicated quiet prayer spaces before and after security.", "Departures and arrivals", true, true));
        facilities.save(facility(dxb, FacilityType.SLEEP, "Sleep & refresh suites", "Bookable sleep pods, showers and rest areas for connecting guests.", "Concourses A and B", true, false));
        facilities.save(facility(dxb, FacilityType.TRANSPORT, "Metro & transfer desk", "24-hour connection assistance with metro, taxi and hotel transfer guidance.", "Arrivals hall", true, true));
        facilities.save(facility(sin, FacilityType.FAMILY, "Family & play zones", "Nursing rooms, play spaces and stroller-friendly facilities.", "Terminals 1–4", true, true));
        facilities.save(facility(sin, FacilityType.MEDICAL, "Raffles Medical Centre", "Urgent medical support and pharmacy access for travellers.", "Terminal 3", true, false));
        facilities.save(facility(sin, FacilityType.WIFI, "Unlimited Wi-Fi", "High-speed Wi-Fi, work desks and device charging throughout the airport.", "All terminals", true, true));
        facilities.save(facility(mle, FacilityType.ACCESSIBILITY, "Assisted journey point", "Wheelchair, reduced-mobility and meet-and-assist coordination.", "Main terminal entrance", true, true));
        facilities.save(facility(mle, FacilityType.TRANSPORT, "Resort transfer centre", "Guidance for seaplane, speedboat and domestic island connections.", "Arrivals concourse", true, true));
    }

    private AirportFacility facility(Airport airport, FacilityType type, String name, String description,
                                     String location, boolean open, boolean complimentary) {
        AirportFacility value = new AirportFacility(); value.setAirport(airport); value.setType(type); value.setName(name);
        value.setDescription(description); value.setLocation(location); value.setOpen24Hours(open); value.setComplimentary(complimentary); return value;
    }

    private void seedPackages() {
        packages.save(packageValue("DXB-CITY-4", "Dubai Skyline Escape", airport("DXB"), PackageType.CITY_BREAK, 4,
                "A polished city break with time for architecture, desert experiences and waterfront dining.", "Five-star hotel · return airport transfer · city tour · breakfast", "Canal Central Dubai", 219000));
        packages.save(packageValue("MLE-REEF-5", "Maldives Reef Retreat", airport("MLE"), PackageType.BEACH, 5,
                "A restorative island stay with lagoon access and a guided reef experience.", "Resort stay · speedboat transfer · half board · reef excursion", "Velassaru-style island resort", 298000));
        packages.save(packageValue("SIN-FAMILY-4", "Singapore Family Discovery", airport("SIN"), PackageType.FAMILY, 4,
                "An easy family itinerary combining gardens, wildlife and waterfront attractions.", "Family room · breakfast · attraction pass · airport transfer", "Village Hotel Sentosa", 264000));
        packages.save(packageValue("BKK-WELL-3", "Bangkok Wellness Weekend", airport("BKK"), PackageType.WELLNESS, 3,
                "A short wellness-led stay with curated dining and a premium spa treatment.", "Boutique hotel · breakfast · spa credit · private transfer", "Siam wellness hotel", 176000));
        packages.save(packageValue("DEL-HERITAGE-5", "Delhi Heritage Journey", airport("DEL"), PackageType.ADVENTURE, 5,
                "Explore landmark architecture, regional cuisine and a day journey to Agra.", "Hotel · daily breakfast · guided tours · rail day trip", "The Metropolitan New Delhi", 184000));
        packages.save(packageValue("DOH-BIZ-3", "Doha Business Plus", airport("DOH"), PackageType.BUSINESS, 3,
                "A high-efficiency business stay with workspace, transfers and flexible checkout.", "Executive room · airport transfer · lounge access · late checkout", "West Bay business hotel", 203000));
    }

    private TravelPackage packageValue(String code, String title, Airport destination, PackageType type, int nights,
                                       String description, String inclusions, String hotel, int price) {
        TravelPackage value = new TravelPackage(); value.setCode(code); value.setTitle(title); value.setDestination(destination);
        value.setType(type); value.setNights(nights); value.setDescription(description); value.setInclusions(inclusions);
        value.setHotel(hotel); value.setPricePerPerson(BigDecimal.valueOf(price)); return value;
    }

    private void seedConnections() {
        LocalDate date = flights.findFirstByFlightNumberIgnoreCase("AN310").map(f -> f.getDepartureAt().toLocalDate()).orElse(LocalDate.now().plusDays(1));
        if (!flights.existsByFlightNumberIgnoreCase("AN845"))
            flights.save(flight("AN845", airport("SIN"), airport("BKK"), plane("4R-ANB"), date, "14:15", 2, 25, "D18", 41600, 104000));
        if (!flights.existsByFlightNumberIgnoreCase("AN760"))
            flights.save(flight("AN760", airport("DXB"), airport("DOH"), plane("4R-ANC"), date, "09:10", 1, 10, "B7", 35200, 89000));
        if (seedLiveFlight && !flights.existsByFlightNumberIgnoreCase("AN900")) {
            Flight live = flight("AN900", airport("CMB"), airport("SIN"), plane("4R-ANC"), LocalDate.now(),
                    LocalTime.now().minusHours(2).withSecond(0).withNano(0).toString(), 4, 5, "7", 77800, 198000);
            live.setStatus(FlightStatus.IN_AIR); flights.save(live);
        }
    }

    private void upgradeOperationalData() {
        coordinate("CMB", "7.180800", "79.884100"); coordinate("DXB", "25.253200", "55.365700");
        coordinate("SIN", "1.364400", "103.991500"); coordinate("MLE", "4.191800", "73.529100");
        coordinate("DEL", "28.556200", "77.100000"); coordinate("BKK", "13.690000", "100.750100");
        coordinate("KUL", "2.745600", "101.707200"); coordinate("DOH", "25.273100", "51.608100");
        var upgradedFlights = flights.findAll();
        upgradedFlights.forEach(value -> {
            if (value.getPremiumEconomyFare() == null) value.setPremiumEconomyFare(value.getEconomyFare().multiply(new BigDecimal("1.45")));
            if (value.getFirstFare() == null) value.setFirstFare(value.getBusinessFare().multiply(new BigDecimal("1.85")));
            value.setFareBrand(fareBrand(value.getFlightNumber()));
            if (value.getBaggageAllowanceKg() == 23) value.setBaggageAllowanceKg(baseBaggage(value.getFlightNumber()));
            if (value.getOnboardFeatures() == null || value.getOnboardFeatures().isBlank())
                value.setOnboardFeatures("Chef-inspired meal · Wi-Fi messaging · USB power");
            if ("AN508".equals(value.getFlightNumber()) && value.getStatus() == FlightStatus.SCHEDULED) {
                value.setStatus(FlightStatus.DELAYED); value.setDelayMinutes(55); value.setDelayReason("Late inbound aircraft");
            }
        });
        flights.saveAll(upgradedFlights);
    }

    private String fareBrand(String flightNumber) {
        return switch (flightNumber) {
            case "AN112", "AN113", "AN612", "AN845" -> "Vista Saver";
            case "AN204", "AN205", "AN508", "AN509" -> "Vista Flex";
            case "AN722", "AN900" -> "Vista Signature";
            default -> "Vista Smart";
        };
    }

    private int baseBaggage(String flightNumber) {
        return switch (flightNumber) {
            case "AN112", "AN113", "AN612", "AN845" -> 20;
            case "AN204", "AN205", "AN508", "AN509", "AN722", "AN900" -> 30;
            default -> 25;
        };
    }

    private void coordinate(String code, String latitude, String longitude) {
        Airport value = airport(code); value.setLatitude(new BigDecimal(latitude)); value.setLongitude(new BigDecimal(longitude));
    }

    private Flight flight(String number, Airport from, Airport to, Aircraft plane, LocalDate day, String time, int hours, int minutes,
                          String gate, int economy, int business) {
        Flight flight = new Flight(); LocalDateTime depart = LocalDateTime.of(day, LocalTime.parse(time));
        flight.setFlightNumber(number); flight.setOrigin(from); flight.setDestination(to); flight.setAircraft(plane);
        flight.setDepartureAt(depart); flight.setArrivalAt(depart.plusHours(hours).plusMinutes(minutes)); flight.setDepartureGate(gate);
        flight.setEconomyFare(BigDecimal.valueOf(economy));
        flight.setPremiumEconomyFare(BigDecimal.valueOf(economy).multiply(new BigDecimal("1.45")));
        flight.setBusinessFare(BigDecimal.valueOf(business)); flight.setFirstFare(BigDecimal.valueOf(business).multiply(new BigDecimal("1.85")));
        flight.setBaggageAllowanceKg(23); flight.setStatus(FlightStatus.SCHEDULED); return flight;
    }

    private Airport airport(String code) { return airports.findByIataCodeIgnoreCase(code).orElseThrow(); }
    private Aircraft plane(String registration) { return aircraft.findByRegistrationIgnoreCase(registration).orElseThrow(); }
}
