package com.aeronest.repository;

import com.aeronest.domain.Payment;
import com.aeronest.domain.DomainEnums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBookingId(Long bookingId);
    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = :status")
    BigDecimal totalByStatus(PaymentStatus status);
}
