package com.aeronest.service;

import com.aeronest.domain.DomainEnums.PaymentMethod;
import com.aeronest.domain.DomainEnums.PaymentStatus;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Component
public class BankTransferPaymentProcessor implements PaymentProcessor {
    public PaymentResult process(BigDecimal amount) { return new PaymentResult(PaymentStatus.PAID, "BANK-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase()); }
    public PaymentMethod supports() { return PaymentMethod.BANK_TRANSFER; }
}
