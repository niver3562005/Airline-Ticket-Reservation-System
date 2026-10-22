package com.aeronest.repository;

import com.aeronest.domain.Passenger;
import com.aeronest.domain.DomainEnums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    List<Passenger> findByFlightIdAndBookingStatusNot(Long flightId, BookingStatus status);
    boolean existsByFlightIdAndSeatNumberIgnoreCaseAndBookingStatusNot(Long flightId, String seatNumber, BookingStatus status);
    long countByFlightId(Long flightId);
    long countByCheckedInTrue();
}
