package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.client.BankClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.PaymentRequest;
import com.checkout.payment.gateway.model.PaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;
  private final BankClient bankClient;

  public PaymentGatewayService(PaymentsRepository paymentsRepository, BankClient bankClient) {
    this.paymentsRepository = paymentsRepository;
    this.bankClient = bankClient;
  }

  public PaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);
    return paymentsRepository.get(id).orElseThrow(() -> new PaymentNotFoundException("Invalid ID"));
  }

  public PaymentResponse processPayment(PaymentRequest paymentRequest) {
    boolean authorized = bankClient.authorize(paymentRequest);

    PaymentResponse response = new PaymentResponse();
    response.setId(UUID.randomUUID());
    response.setStatus(authorized ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED);
    response.setCardNumberLastFour(getLastFourDigits(paymentRequest.getCardNumber()));
    response.setExpiryMonth(paymentRequest.getExpiryMonth());
    response.setExpiryYear(paymentRequest.getExpiryYear());
    response.setCurrency(paymentRequest.getCurrency());
    response.setAmount(paymentRequest.getAmount());

    paymentsRepository.add(response);
    return response;
  }

  private String getLastFourDigits(String cardNumber) {
    return cardNumber.substring(cardNumber.length() - 4);
  }
}
