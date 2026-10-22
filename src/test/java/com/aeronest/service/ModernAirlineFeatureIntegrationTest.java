package com.aeronest.service;

import com.aeronest.domain.Booking;
import com.aeronest.domain.DomainEnums.CabinClass;
import com.aeronest.domain.DomainEnums.DisruptionChoice;
import com.aeronest.domain.DomainEnums.DisruptionStatus;
import com.aeronest.domain.DomainEnums.PartnerType;
import com.aeronest.domain.DomainEnums.PaymentMethod;
import com.aeronest.domain.DomainEnums.JourneyServiceType;
import com.aeronest.domain.DomainEnums.FlightStatus;
import com.aeronest.domain.DomainEnums.NotificationChannel;
import com.aeronest.repository.FlightRepository;
import com.aeronest.repository.JourneyServiceRequestRepository;
import com.aeronest.repository.PartnerReservationRepository;
import com.aeronest.repository.TravelNotificationRepository;
import com.aeronest.repository.RefundRequestRepository;
import com.aeronest.web.BookingForm;
import com.aeronest.web.PartnerReservationForm;
import com.aeronest.web.PassengerForm;
import com.aeronest.web.JourneyServiceForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ModernAirlineFeatureIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired TravelPartnerService partners;
    @Autowired PartnerReservationRepository reservations;
    @Autowired TravelNotificationRepository notifications;
    @Autowired FlightRepository flightRepository;
    @Autowired FlightAvailabilityService availability;
    @Autowired FlightTelemetryService telemetry;
    @Autowired BookingService bookingService;
    @Autowired FlightService flightService;
    @Autowired DisruptionService disruptions;
    @Autowired SeatMapService seats;
    @Autowired RefundRequestRepository refunds;
    @Autowired JourneyServiceRequestService journeyServices;
    @Autowired JourneyServiceRequestRepository journeyServiceRequests;
    @Autowired RefundService refundService;

    @Test void manageBookingRendersPremiumPriorityAirportCareAndFlightIdentity() throws Exception {
        var flight = flightRepository.findAllByOrderByDepartureAtAsc().stream()
                .filter(value -> value.getDepartureAt().isAfter(LocalDateTime.now().plusHours(4))).findFirst().orElseThrow();
        Booking booking = bookingService.create(bookingForm(flight.getId(), seats.availableSeats(flight, CabinClass.ECONOMY).get(0)));

        mvc.perform(post("/manage").with(csrf()).param("pnr", booking.getPnr()).param("email", booking.getContactEmail()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Move through the airport beautifully")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("aerovista-flight-logo.svg")));
    }

    @Test void priorityAirportCareIsPersistedAndNotifiesEveryPassengerChannel() {
        var flight = flightRepository.findAllByOrderByDepartureAtAsc().stream()
                .filter(value -> value.getDepartureAt().isAfter(LocalDateTime.now().plusHours(4))).findFirst().orElseThrow();
        Booking booking = bookingService.create(bookingForm(flight.getId(), seats.availableSeats(flight, CabinClass.ECONOMY).get(0)));
        JourneyServiceForm form = new JourneyServiceForm(); form.setType(JourneyServiceType.FAST_TRACK_SECURITY);
        form.setPassengerId(booking.getPassengers().get(0).getId()); form.setNotes("Travelling with an elderly parent");

        var saved = journeyServices.request(booking.getPnr(), booking.getContactEmail(), form);

        assertThat(saved.getReference()).startsWith("PR-");
        assertThat(saved.getPrice()).isEqualByComparingTo("6500.00");
        assertThat(journeyServiceRequests.findByBookingIdOrderByCreatedAtDesc(booking.getId())).hasSize(1);
        assertThat(notifications.findTop50ByOrderByCreatedAtDesc())
                .filteredOn(item -> item.getTitle().contains(saved.getReference())).hasSize(2);
    }

    @Test void reservesAirportServiceAndCreatesEmailAndSmsNotifications() {
        var cab = partners.partners(PartnerType.CAB).get(0);
        PartnerReservationForm form = new PartnerReservationForm(); form.setPartnerId(cab.getId());
        form.setTravellerName("Nivetha Silva"); form.setEmail("nivetha@example.com"); form.setPhone("+94 77 123 4567");
        form.setServiceAt(LocalDateTime.now().plusDays(2)); form.setGuests(2); form.setPickupLocation("CMB arrivals");
        form.setDropoffLocation("Colombo 03");

        var saved = partners.reserve(form, null);

        assertThat(saved.getReference()).startsWith("SV-");
        assertThat(reservations.findByReferenceIgnoreCase(saved.getReference())).isPresent();
        assertThat(notifications.findTop50ByOrderByCreatedAtDesc()).filteredOn(item -> item.getTitle().contains(saved.getReference())).hasSize(2);
    }

    @Test void exposesFourCabinsAndPersistentTelemetryForEveryFlight() {
        var flight = flightRepository.findFirstByFlightNumberIgnoreCase("AN204").orElseThrow();
        assertThat(availability.forFlight(flight)).extracting(item -> item.cabin()).containsExactly(CabinClass.ECONOMY, CabinClass.PREMIUM_ECONOMY, CabinClass.BUSINESS, CabinClass.FIRST);
        assertThat(telemetry.snapshot(flight).getNearestLocation()).isEqualTo("Colombo");
    }

    @Test void flightCancellationCreatesPassengerRebookingCreditAndRefundChoices() {
        var flight = flightRepository.findAllByOrderByDepartureAtAsc().stream()
                .filter(value -> value.getDepartureAt().isAfter(LocalDateTime.now().plusHours(4))).findFirst().orElseThrow();
        String seat = seats.availableSeats(flight, CabinClass.ECONOMY).get(0);
        Booking booking = bookingService.create(bookingForm(flight.getId(), seat));

        flightService.updateStatus(flight.getId(), com.aeronest.domain.DomainEnums.FlightStatus.CANCELLED);
        var disruption = disruptions.forBooking(booking);
        assertThat(disruption).isNotNull(); assertThat(disruption.getStatus()).isEqualTo(DisruptionStatus.ACTION_REQUIRED);

        disruptions.select(booking.getPnr(), booking.getContactEmail(), DisruptionChoice.FULL_REFUND, null);
        assertThat(disruptions.forBooking(booking).getPassengerChoice()).isEqualTo(DisruptionChoice.FULL_REFUND);
        assertThat(refunds.findByBookingId(booking.getId()).orElseThrow().getCancellationFee()).isEqualByComparingTo("0.00");
        assertThat(refunds.findByBookingId(booking.getId()).orElseThrow().getRefundableAmount()).isEqualByComparingTo(booking.getTotalAmount());
    }

    @Test void completesARebookingWithAProtectedCabinMatchedSeat() {
        var original = flightRepository.findFirstByFlightNumberIgnoreCase("AN204").orElseThrow();
        var alternate = new com.aeronest.domain.Flight(); alternate.setFlightNumber("AN294"); alternate.setOrigin(original.getOrigin());
        alternate.setDestination(original.getDestination()); alternate.setAircraft(original.getAircraft());
        alternate.setDepartureAt(original.getDepartureAt().plusDays(2)); alternate.setArrivalAt(original.getArrivalAt().plusDays(2));
        alternate.setStatus(com.aeronest.domain.DomainEnums.FlightStatus.SCHEDULED); alternate.setDepartureGate("8");
        alternate.setEconomyFare(original.getEconomyFare()); alternate.setPremiumEconomyFare(original.getPremiumEconomyFare());
        alternate.setBusinessFare(original.getBusinessFare()); alternate.setFirstFare(original.getFirstFare()); alternate.setBaggageAllowanceKg(23);
        alternate = flightRepository.save(alternate);
        Booking booking = bookingService.create(bookingForm(original.getId(), seats.availableSeats(original, CabinClass.ECONOMY).get(0)));

        flightService.updateStatus(original.getId(), com.aeronest.domain.DomainEnums.FlightStatus.CANCELLED);
        var disruption = disruptions.select(booking.getPnr(), booking.getContactEmail(), DisruptionChoice.REBOOK, alternate.getId());
        disruptions.complete(disruption.getId());

        assertThat(booking.getFlight().getId()).isEqualTo(alternate.getId());
        assertThat(booking.getPassengers().get(0).getFlight().getId()).isEqualTo(alternate.getId());
        assertThat(seats.cabinFor(alternate, booking.getPassengers().get(0).getSeatNumber())).isEqualTo(CabinClass.ECONOMY);
        assertThat(disruption.getStatus()).isEqualTo(DisruptionStatus.COMPLETED);
    }

    @Test void qualifyingCancellationShowsAFullCashReturnBeforeSubmission() {
        var flight = flightRepository.findAllByOrderByDepartureAtAsc().stream()
                .filter(value -> value.getDepartureAt().isAfter(LocalDateTime.now().plusDays(3))).findFirst().orElseThrow();
        Booking booking = bookingService.create(bookingForm(flight.getId(), seats.availableSeats(flight, CabinClass.ECONOMY).get(0)));

        var quote = refundService.quote(booking);

        assertThat(quote.fullRefund()).isTrue();
        assertThat(quote.fee()).isEqualByComparingTo("0.00");
        assertThat(quote.refundable()).isEqualByComparingTo(booking.getTotalAmount());
    }

    @Test void delayUpdateCreatesAnEmailAlertWithRevisedTimeAndReason() {
        var flight = flightRepository.findAllByOrderByDepartureAtAsc().stream()
                .filter(value -> value.getDepartureAt().isAfter(LocalDateTime.now().plusHours(4)))
                .filter(value -> value.getStatus() == FlightStatus.SCHEDULED).findFirst().orElseThrow();
        Booking booking = bookingService.create(bookingForm(flight.getId(), seats.availableSeats(flight, CabinClass.ECONOMY).get(0)));
        flight.setDelayMinutes(70); flight.setDelayReason("Weather at the destination airport");

        flightService.updateStatus(flight.getId(), FlightStatus.DELAYED);

        assertThat(notifications.findTop50ByOrderByCreatedAtDesc())
                .filteredOn(item -> item.getChannel() == NotificationChannel.EMAIL && item.getTitle().contains(flight.getFlightNumber()))
                .anySatisfy(item -> assertThat(item.getMessage()).contains("70 minutes").contains("Weather at the destination airport"));
    }

    private BookingForm bookingForm(Long flightId, String seat) {
        PassengerForm passenger = new PassengerForm(); passenger.setFirstName("Nivetha"); passenger.setLastName("Silva");
        passenger.setDateOfBirth(LocalDate.of(2000, 4, 12)); passenger.setNationality("Sri Lankan");
        passenger.setPassportNumber("N1234567"); passenger.setSeatNumber(seat); passenger.setCabinClass(CabinClass.ECONOMY);
        BookingForm form = new BookingForm(); form.setFlightId(flightId); form.setContactName("Nivetha Silva");
        form.setContactEmail("nivetha@example.com"); form.setContactPhone("+94 77 123 4567"); form.setPaymentMethod(PaymentMethod.CARD);
        form.getPassengers().add(passenger); return form;
    }
}
