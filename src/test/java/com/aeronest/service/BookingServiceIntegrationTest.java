package com.aeronest.service;

import com.aeronest.domain.Booking;
import com.aeronest.domain.Flight;
import com.aeronest.domain.DomainEnums.BookingStatus;
import com.aeronest.domain.DomainEnums.CabinClass;
import com.aeronest.domain.DomainEnums.PaymentMethod;
import com.aeronest.domain.DomainEnums.PaymentStatus;
import com.aeronest.domain.DomainEnums.RefundStatus;
import com.aeronest.repository.FlightRepository;
import com.aeronest.repository.PaymentRepository;
import com.aeronest.repository.RefundRequestRepository;
import com.aeronest.repository.TravelNotificationRepository;
import com.aeronest.web.BookingForm;
import com.aeronest.web.PassengerForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BookingServiceIntegrationTest {
    @Autowired BookingService bookings;
    @Autowired FlightRepository flights;
    @Autowired PaymentRepository payments;
    @Autowired RefundRequestRepository refunds;
    @Autowired TravelNotificationRepository notifications;
    @Autowired SeatMapService seats;

    @Test void createsConfirmedBookingPassengerAndPaymentAtomically() {
        Flight flight = flights.findAllByOrderByDepartureAtAsc().get(0);
        String seat = seats.availableSeats(flight, CabinClass.ECONOMY).get(0);
        Booking booking = bookings.create(validForm(flight, seat, PaymentMethod.CARD, null));
        assertThat(booking.getPnr()).hasSize(6);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getPassengers()).hasSize(1);
        assertThat(booking.getPassengers().get(0).getSeatNumber()).isEqualTo(seat);
        assertThat(payments.findByBookingId(booking.getId())).isPresent();
    }

    @Test void refusesASeatThatIsAlreadyBooked() {
        Flight flight = flights.findAllByOrderByDepartureAtAsc().get(1);
        String seat = seats.availableSeats(flight, CabinClass.ECONOMY).get(0);
        bookings.create(validForm(flight, seat, PaymentMethod.CARD, null));
        assertThatThrownBy(() -> bookings.create(validForm(flight, seat, PaymentMethod.CARD, null)))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("Seat");
    }

    @Test void payAtCounterCreatesPendingBooking() {
        Flight flight = flights.findAllByOrderByDepartureAtAsc().get(2);
        String seat = seats.availableSeats(flight, CabinClass.ECONOMY).get(0);
        Booking booking = bookings.create(validForm(flight, seat, PaymentMethod.PAY_AT_COUNTER, null));
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test void promoCodeReducesPriceByTenPercent() {
        Flight flight = flights.findAllByOrderByDepartureAtAsc().get(3);
        String seat = seats.availableSeats(flight, CabinClass.ECONOMY).get(0);
        Booking booking = bookings.create(validForm(flight, seat, PaymentMethod.CARD, "FLY10"));
        assertThat(booking.getTotalAmount()).isEqualByComparingTo(flight.getEconomyFare().multiply(new BigDecimal("0.90")));
    }

    @Test void rejectsUnknownPromoCode() {
        Flight flight = flights.findAllByOrderByDepartureAtAsc().get(4);
        String seat = seats.availableSeats(flight, CabinClass.ECONOMY).get(0);
        assertThatThrownBy(() -> bookings.create(validForm(flight, seat, PaymentMethod.CARD, "NOTREAL")))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("promo code");
    }

    @Test void cancellationStartsRefundAndMultiChannelNotificationWorkflow() {
        Flight flight = flights.findAllByOrderByDepartureAtAsc().stream()
                .filter(value -> value.getDepartureAt().isAfter(java.time.LocalDateTime.now().plusHours(4)))
                .reduce((first, second) -> second).orElseThrow();
        String seat = seats.availableSeats(flight, CabinClass.ECONOMY).get(0);
        Booking booking = bookings.create(validForm(flight, seat, PaymentMethod.CARD, null));

        bookings.cancel(booking.getPnr(), booking.getContactEmail(), "Schedule conflict");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(payments.findByBookingId(booking.getId()).orElseThrow().getStatus()).isEqualTo(PaymentStatus.REFUND_PENDING);
        assertThat(refunds.findByBookingId(booking.getId())).get().extracting("status").isEqualTo(RefundStatus.REQUESTED);
        assertThat(notifications.findTop50ByOrderByCreatedAtDesc()).hasSizeGreaterThanOrEqualTo(4);
    }

    private BookingForm validForm(Flight flight, String seat, PaymentMethod method, String promo) {
        PassengerForm passenger = new PassengerForm(); passenger.setFirstName("Nivetha"); passenger.setLastName("Silva");
        passenger.setDateOfBirth(LocalDate.of(2000, 4, 12)); passenger.setNationality("Sri Lankan");
        passenger.setPassportNumber("N1234567"); passenger.setSeatNumber(seat); passenger.setCabinClass(CabinClass.ECONOMY);
        BookingForm form = new BookingForm(); form.setFlightId(flight.getId()); form.setContactName("Nivetha Silva");
        form.setContactEmail("nivetha@example.com"); form.setContactPhone("+94 77 123 4567"); form.setPaymentMethod(method); form.setPromoCode(promo);
        form.getPassengers().add(passenger); return form;
    }
}
