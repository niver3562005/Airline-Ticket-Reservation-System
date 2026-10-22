package com.aeronest.service;

import com.aeronest.domain.DomainEnums.PaymentMethod;
import com.aeronest.domain.DomainEnums.PaymentStatus;
import java.math.BigDecimal;

public interface PaymentProcessor {
    PaymentResult process(BigDecimal amount);
    PaymentMethod supports();
    record PaymentResult(PaymentStatus status, String reference) {}
}
