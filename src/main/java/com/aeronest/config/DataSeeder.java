package com.aeronest.config;

import com.aeronest.domain.Aircraft;
import com.aeronest.domain.Airport;
import com.aeronest.domain.Flight;
import com.aeronest.domain.DomainEnums.FlightStatus;
import com.aeronest.repository.AircraftRepository;
import com.aeronest.repository.AirportRepository;
import com.aeronest.repository.FlightRepository;
import com.aeronest.repository.CustomerAccountRepository;
import com.aeronest.domain.CustomerAccount;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {
    private final AirportRepository airports; private final AircraftRepository aircraft; private final FlightRepository flights;
    private final CustomerAccountRepository accounts; private final PasswordEncoder encoder;
    public DataSeeder(AirportRepository airports, AircraftRepository aircraft, FlightRepository flights,
                      CustomerAccountRepository accounts, PasswordEncoder encoder) {
        this.airports = airports; this.aircraft = aircraft; this.flights = flights; this.accounts = accounts; this.encoder = encoder;
    }
    @Override public void run(String... args) {
        if (airports.count() == 0) seedAirports();
        if (aircraft.count() == 0) seedAircraft();
        if (flights.count() == 0) seedFlights();
        if (accounts.count() == 0) seedAccount();
    }
    private void seedAccount() {
        CustomerAccount account = new CustomerAccount(); account.setFirstName("Asha"); account.setLastName("Perera");
        account.setEmail("traveller@aerovista.test"); account.setPhone("+94 77 555 0148");
        account.setPasswordHash(encoder.encode("Travel@123")); account.setLoyaltyNumber("AV00001024");
        account.setEmailVerified(true); account.setMarketingOptIn(true); account.setLoyaltyPoints(2450); accounts.save(account);
    }
    private void seedAirports() {
        airports.saveAll(List.of(
                airport("CMB", "Bandaranaike International Airport", "Colombo", "Sri Lanka", "Asia/Colombo", "7.180800", "79.884100"),
                airport("DXB", "Dubai International Airport", "Dubai", "United Arab Emirates", "Asia/Dubai", "25.253200", "55.365700"),
                airport("SIN", "Singapore Changi Airport", "Singapore", "Singapore", "Asia/Singapore", "1.364400", "103.991500"),
                airport("MLE", "Velana International Airport", "Malé", "Maldives", "Indian/Maldives", "4.191800", "73.529100"),
                airport("DEL", "Indira Gandhi International Airport", "Delhi", "India", "Asia/Kolkata", "28.556200", "77.100000"),
                airport("BKK", "Suvarnabhumi Airport", "Bangkok", "Thailand", "Asia/Bangkok", "13.690000", "100.750100"),
                airport("KUL", "Kuala Lumpur International Airport", "Kuala Lumpur", "Malaysia", "Asia/Kuala_Lumpur", "2.745600", "101.707200"),
                airport("DOH", "Hamad International Airport", "Doha", "Qatar", "Asia/Qatar", "25.273100", "51.608100")
        ));
    }
    private Airport airport(String code, String name, String city, String country, String zone, String latitude, String longitude) {
        Airport a = new Airport(); a.setIataCode(code); a.setName(name); a.setCity(city); a.setCountry(country); a.setTimeZone(zone);
        a.setLatitude(new BigDecimal(latitude)); a.setLongitude(new BigDecimal(longitude)); return a;
    }
    private void seedAircraft() {
        aircraft.saveAll(List.of(
                craft("4R-ANA", "Airbus A320neo", 0, 2, 2, 16, 6),
                craft("4R-ANB", "Airbus A321LR", 0, 3, 3, 21, 6),
                craft("4R-ANC", "Boeing 787-8 Dreamliner", 2, 4, 4, 25, 8)
        ));
    }
    private Aircraft craft(String reg, String model, int first, int business, int premium, int economy, int perRow) {
        Aircraft a = new Aircraft(); a.setRegistration(reg); a.setModel(model); a.setFirstRows(first); a.setBusinessRows(business);
        a.setPremiumEconomyRows(premium); a.setEconomyRows(economy); a.setSeatsPerRow(perRow); return a;
    }
    private void seedFlights() {
        Airport cmb = code("CMB"), dxb = code("DXB"), sin = code("SIN"), mle = code("MLE"), del = code("DEL"), bkk = code("BKK"), kul = code("KUL"), doh = code("DOH");
        Aircraft a320 = reg("4R-ANA"), a321 = reg("4R-ANB"), b787 = reg("4R-ANC");
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        flights.saveAll(List.of(
                flight("AN204", cmb, dxb, b787, tomorrow, "02:10", 4, 40, "4", 89500, 238000),
                flight("AN310", cmb, sin, a321, tomorrow, "07:35", 4, 5, "7", 74200, 189000),
                flight("AN112", cmb, mle, a320, tomorrow, "10:20", 1, 30, "2", 38900, 92000),
                flight("AN416", cmb, del, a321, tomorrow, "14:45", 3, 35, "9", 56200, 141000),
                flight("AN508", cmb, bkk, a321, tomorrow.plusDays(2), "01:30", 3, 45, "5", 68100, 169000),
                flight("AN612", cmb, kul, a320, tomorrow.plusDays(2), "08:15", 3, 40, "6", 62400, 154000),
                flight("AN722", cmb, doh, b787, tomorrow.plusDays(3), "03:25", 5, 0, "3", 92700, 246000),
                flight("AN205", dxb, cmb, b787, tomorrow.plusDays(4), "18:10", 4, 35, "B12", 90500, 241000),
                flight("AN311", sin, cmb, a321, tomorrow.plusDays(5), "21:20", 4, 0, "D4", 76500, 194000),
                flight("AN113", mle, cmb, a320, tomorrow.plusDays(6), "16:05", 1, 25, "1", 40100, 95000),
                flight("AN417", del, cmb, a321, tomorrow.plusDays(7), "06:50", 3, 30, "14", 57900, 144000),
                flight("AN509", bkk, cmb, a321, tomorrow.plusDays(8), "11:40", 3, 50, "C8", 69700, 173000)
        ));
    }
    private Airport code(String value) { return airports.findByIataCodeIgnoreCase(value).orElseThrow(); }
    private Aircraft reg(String value) { return aircraft.findByRegistrationIgnoreCase(value).orElseThrow(); }
    private Flight flight(String number, Airport from, Airport to, Aircraft plane, LocalDate day, String time, int h, int m,
                          String gate, int economy, int business) {
        Flight f = new Flight(); LocalDateTime depart = LocalDateTime.of(day, LocalTime.parse(time));
        f.setFlightNumber(number); f.setOrigin(from); f.setDestination(to); f.setAircraft(plane); f.setDepartureAt(depart);
        f.setArrivalAt(depart.plusHours(h).plusMinutes(m)); f.setDepartureGate(gate); f.setEconomyFare(BigDecimal.valueOf(economy));
        f.setPremiumEconomyFare(BigDecimal.valueOf(economy).multiply(new BigDecimal("1.45")));
        f.setBusinessFare(BigDecimal.valueOf(business)); f.setFirstFare(BigDecimal.valueOf(business).multiply(new BigDecimal("1.85")));
        f.setBaggageAllowanceKg(23); f.setStatus(FlightStatus.SCHEDULED); return f;
    }
}
