package com.checkout.payment.gateway.repository;

import com.checkout.payment.gateway.model.PaymentResponse;
import java.util.Optional;
import java.util.UUID;

public interface PaymentsRepository {

  public void add(PaymentResponse payment);

  public Optional<PaymentResponse> get(UUID id);
}
