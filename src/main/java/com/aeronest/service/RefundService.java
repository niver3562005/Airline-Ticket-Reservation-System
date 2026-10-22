package com.aeronest.service;

import com.aeronest.domain.Booking;
import com.aeronest.domain.Payment;
import com.aeronest.domain.RefundRequest;
import com.aeronest.domain.DomainEnums.PaymentStatus;
import com.aeronest.domain.DomainEnums.RefundStatus;
import com.aeronest.repository.PaymentRepository;
import com.aeronest.repository.RefundRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RefundService {
    private final RefundRequestRepository refunds;
    private final PaymentRepository payments;
    private final NotificationService notifications;

    public RefundService(RefundRequestRepository refunds, PaymentRepository payments, NotificationService notifications) {
        this.refunds = refunds; this.payments = payments; this.notifications = notifications;
    }

    @Transactional
    public RefundRequest request(Booking booking, String reason) {
        return refunds.findByBookingId(booking.getId()).orElseGet(() -> {
            Payment payment = payments.findByBookingId(booking.getId()).orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
            boolean airlineDisruption = reason != null && reason.toLowerCase().contains("airline flight cancellation");
            RefundQuote quote = quote(booking, payment.getAmount(), airlineDisruption);
            RefundRequest request = new RefundRequest(); request.setBooking(booking); request.setPayment(payment);
            request.setRequestedAmount(payment.getAmount()); request.setCancellationFee(quote.fee());
            request.setRefundableAmount(quote.refundable()); request.setReason(reason == null || reason.isBlank() ? "Travel plans changed" : reason.trim());
            request.setStatus(payment.getStatus() == PaymentStatus.PAID ? RefundStatus.REQUESTED : RefundStatus.DECLINED);
            if (payment.getStatus() == PaymentStatus.PAID) payment.setStatus(PaymentStatus.REFUND_PENDING);
            RefundRequest saved = refunds.save(request);
            notifications.refundRequested(booking, saved.getRefundableAmount().toPlainString()); return saved;
        });
    }

    public RefundQuote quote(Booking booking) {
        Payment payment = payments.findByBookingId(booking.getId()).orElse(null);
        return quote(booking, payment == null ? booking.getTotalAmount() : payment.getAmount(), false);
    }

    private RefundQuote quote(Booking booking, BigDecimal paid, boolean airlineDisruption) {
        LocalDateTime now = LocalDateTime.now();
        boolean coolingOff = booking.getCreatedAt() != null && booking.getCreatedAt().isAfter(now.minusHours(24))
                && booking.getFlight().getDepartureAt().isAfter(now.plusHours(72));
        BigDecimal rate = airlineDisruption || coolingOff ? BigDecimal.ZERO
                : booking.getFlight().getDepartureAt().isAfter(now.plusDays(7)) ? new BigDecimal("0.05") : new BigDecimal("0.15");
        BigDecimal fee = paid.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        String policy = airlineDisruption ? "Full refund because the airline cancelled the flight"
                : coolingOff ? "Full refund within the 24-hour cooling-off period"
                : rate.compareTo(new BigDecimal("0.05")) == 0 ? "95% cash refund more than seven days before departure"
                : "85% cash refund between four hours and seven days before departure";
        return new RefundQuote(paid, fee, paid.subtract(fee), policy, coolingOff || airlineDisruption);
    }

    public record RefundQuote(BigDecimal paid, BigDecimal fee, BigDecimal refundable, String policy, boolean fullRefund) {}

    public List<RefundRequest> all() { return refunds.findAllByOrderByCreatedAtDesc(); }

    public long pendingCount() { return refunds.countByStatusIn(List.of(RefundStatus.REQUESTED, RefundStatus.APPROVED, RefundStatus.PROCESSING)); }

    @Transactional
    public void complete(Long id) {
        RefundRequest request = refunds.findById(id).orElseThrow(() -> new ResourceNotFoundException("Refund request not found"));
        if (request.getStatus() == RefundStatus.COMPLETED) throw new BusinessRuleException("This refund is already complete");
        if (request.getStatus() == RefundStatus.DECLINED) throw new BusinessRuleException("A declined refund cannot be completed");
        request.setStatus(RefundStatus.COMPLETED); request.setProcessedAt(LocalDateTime.now());
        request.setProcessorReference("RF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        request.getPayment().setStatus(PaymentStatus.REFUNDED);
        notifications.refundCompleted(request.getBooking(), request.getRefundableAmount().toPlainString(), request.getProcessorReference());
    }
}
