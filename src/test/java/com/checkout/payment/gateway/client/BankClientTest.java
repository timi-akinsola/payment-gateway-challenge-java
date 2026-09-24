package com.checkout.payment.gateway.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.checkout.payment.gateway.exception.BankServiceException;
import com.checkout.payment.gateway.model.PaymentRequest;

@SuppressWarnings("null") // Null type safety checks already actioned in the respective classes.
@ExtendWith(MockitoExtension.class)
public class BankClientTest {
  private static final String BANK_URL = "http://localhost:8080/payments";

  @Mock 
  private RestTemplate restTemplate;

  private BankClient bankClient;

  @BeforeEach 
  void setUp() {
    bankClient = new BankClient(restTemplate, BANK_URL);
  }

  @Test 
  void authorizeReturnsTrueWhenBankAuthorizes() {
    when(restTemplate.postForObject(eq(BANK_URL), any(), eq(BankPaymentResponse.class)))
    .thenReturn(new BankPaymentResponse(true));

    assertTrue(bankClient.authorize(validRequest()));
  }
  
  @Test 
  void authorizeReturnsFalseWhenBankDeclines() {
    when(restTemplate.postForObject(eq(BANK_URL), any(), eq(BankPaymentResponse.class)))
    .thenReturn(new BankPaymentResponse(false));

    assertFalse(bankClient.authorize(validRequest()));
  }

  @Test 
  void authorizeThrowsWhenBankIsUnreachable() {
    when(restTemplate.postForObject(eq(BANK_URL), any(), eq(BankPaymentResponse.class)))
    .thenThrow(new ResourceAccessException("Connection refused."));

    assertThrows(BankServiceException.class, () -> bankClient.authorize(validRequest()));
  }
  
  @Test 
  void authorizeThrowsWhenBankReturnsNull() {
    when(restTemplate.postForObject(eq(BANK_URL), any(), eq(BankPaymentResponse.class)))
    .thenReturn(null);

    assertThrows(BankServiceException.class, () -> bankClient.authorize(validRequest()));
  }

  @Test 
  void authorizePadsExpiryDateWithLeadingZeroesToBank() {
    when(restTemplate.postForObject(eq(BANK_URL), any(), eq(BankPaymentResponse.class)))
    .thenReturn(new BankPaymentResponse(true));

    ArgumentCaptor<BankPaymentRequest> captor = ArgumentCaptor.forClass(BankPaymentRequest.class);

    PaymentRequest request = validRequest();
    request.setExpiryMonth(9);
    bankClient.authorize(request);

    verify(restTemplate).postForObject(eq(BANK_URL), captor.capture(), eq(BankPaymentResponse.class));
    assertTrue(captor.getValue().getExpiryDate().startsWith("09/"));
  }

  private PaymentRequest validRequest() {
    PaymentRequest request = new PaymentRequest();
    request.setCardNumber("012345678987654");
    request.setExpiryMonth(12);
    request.setExpiryYear(2027);
    request.setCurrency("GBP");
    request.setAmount(5525);
    request.setCvv("001");
    return request;
  }
}
