package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.model.PaymentRequest;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BankPaymentRequest {

  @JsonProperty("card_number")
  private final String cardNumber;
  @JsonProperty("expiry_date")
  private final String expiryDate;
  private final String currency;
  private final int amount;
  private final String cvv;

  private BankPaymentRequest(String cardNumber, String expiryDate, String currency, int amount, String cvv) {
    this.cardNumber = cardNumber;
    this.expiryDate = expiryDate;
    this.currency = currency;
    this.amount = amount;
    this.cvv = cvv;
  }

  public static BankPaymentRequest from(PaymentRequest request) {
    String expiryDate = String.format("%02d/%d", request.getExpiryMonth(), request.getExpiryYear());
    return new BankPaymentRequest(request.getCardNumber(), expiryDate, request.getCurrency(), request.getAmount(), request.getCvv());
  }

  public String getCardNumber() {
    return cardNumber;
  }
  
  public String getExpiryDate() {
    return expiryDate;
  }

  public String getCurrency() {
    return currency;
  }

  public int getAmount() {
    return amount;
  }

  public String getCvv() {
    return cvv;
  }

  @Override
  public String toString() {
    return "BankPaymentRequest{" +
        "cardNumber=" + cardNumber +
        ", expiryDate=" + expiryDate +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        '}';
  }
}
