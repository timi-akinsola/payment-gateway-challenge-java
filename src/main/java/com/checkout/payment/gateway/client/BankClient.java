package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.exception.BankServiceException;
import com.checkout.payment.gateway.model.PaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class BankClient {
  private final RestTemplate restTemplate;
  @NonNull private final String bankUrl;

  public BankClient(
      RestTemplate restTemplate, @Value("${bank.simulator.url}") @NonNull String bankUrl) {
    this.restTemplate = restTemplate;
    this.bankUrl = bankUrl;
  }

  public boolean authorize(PaymentRequest request) {
    BankPaymentRequest bankPaymentRequest = BankPaymentRequest.from(request);
    BankPaymentResponse response;
    try {
      response = restTemplate.postForObject(bankUrl, bankPaymentRequest, BankPaymentResponse.class);

    } catch (RestClientException e) {
      throw new BankServiceException(e.getMessage());
    }
    if (response == null) {
      throw new BankServiceException("Bank returned an empty response");
    }
    return response.isAuthorized();
  }
}
