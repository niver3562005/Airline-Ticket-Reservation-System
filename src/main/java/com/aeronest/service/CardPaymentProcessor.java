package com.aeronest.service;

import com.aeronest.domain.DomainEnums.PaymentMethod;
import com.aeronest.domain.DomainEnums.PaymentStatus;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Component
public class CardPaymentProcessor implements PaymentProcessor {
    public PaymentResult process(BigDecimal amount) { return new PaymentResult(PaymentStatus.PAID, "CARD-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase()); }
    public PaymentMethod supports() { return PaymentMethod.CARD; }
}
