package com.checkout.payment.gateway.repository;

import com.checkout.payment.gateway.model.PaymentResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPaymentsRepository implements PaymentsRepository {

  private final ConcurrentHashMap<UUID, PaymentResponse> payments = new ConcurrentHashMap<>();

  @Override
  public void add(PaymentResponse payment) {
    payments.put(payment.getId(), payment);
  }

  @Override
  public Optional<PaymentResponse> get(UUID id) {
    return Optional.ofNullable(payments.get(id));
  }
}