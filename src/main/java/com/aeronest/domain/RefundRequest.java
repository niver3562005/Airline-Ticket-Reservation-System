package com.aeronest.domain;

import com.aeronest.domain.DomainEnums.RefundStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class RefundRequest extends BaseEntity {
    @OneToOne(fetch = FetchType.EAGER) @JoinColumn(nullable = false, unique = true) private Booking booking;
    @OneToOne(fetch = FetchType.EAGER) @JoinColumn(nullable = false) private Payment payment;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal requestedAmount;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal cancellationFee;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal refundableAmount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private RefundStatus status;
    @Column(nullable = false, length = 300) private String reason;
    private LocalDateTime processedAt;
    @Column(length = 80) private String processorReference;

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }
    public BigDecimal getCancellationFee() { return cancellationFee; }
    public void setCancellationFee(BigDecimal cancellationFee) { this.cancellationFee = cancellationFee; }
    public BigDecimal getRefundableAmount() { return refundableAmount; }
    public void setRefundableAmount(BigDecimal refundableAmount) { this.refundableAmount = refundableAmount; }
    public RefundStatus getStatus() { return status; }
    public void setStatus(RefundStatus status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public String getProcessorReference() { return processorReference; }
    public void setProcessorReference(String processorReference) { this.processorReference = processorReference; }
}
