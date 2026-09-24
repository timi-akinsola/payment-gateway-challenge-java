package com.checkout.payment.gateway.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BankPaymentResponse {
    
  private final boolean authorized;

  @JsonCreator 
  public BankPaymentResponse(@JsonProperty("authorized") boolean authorized) {
    this.authorized = authorized;    
  }

  public boolean isAuthorized() {
    return authorized;
  }
  
  @Override
  public String toString() {
    return "BankPaymentResponse{" +
        "authorized=" + authorized +
        '}';
  }
}
