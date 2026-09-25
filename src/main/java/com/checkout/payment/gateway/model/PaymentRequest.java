package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.time.YearMonth;

public class PaymentRequest implements Serializable {

  @JsonProperty("card_number")
  @NotBlank 
  @Pattern(regexp = "\\d{14,19}")
  private String cardNumber;
  @JsonProperty("expiry_month")
  @NotNull
  @Min(1)
  @Max(12)
  private Integer expiryMonth;
  @JsonProperty("expiry_year")
  @NotNull
  private Integer expiryYear;
  @NotBlank
  @Pattern(regexp = "USD|GBP|EUR", message = "must be one of: USD, GBP, EUR")
  private String currency;
  @Positive
  private int amount;
  @NotBlank
  @Pattern(regexp = "\\d{3,4}")
  private String cvv;

  public String getCardNumber() {
    return cardNumber;
  }

  public void setCardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
  }

  public int getExpiryMonth() {
    return expiryMonth;
  }

  public void setExpiryMonth(int expiryMonth) {
    this.expiryMonth = expiryMonth;
  }

  public int getExpiryYear() {
    return expiryYear;
  }

  public void setExpiryYear(int expiryYear) {
    this.expiryYear = expiryYear;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public String getCvv() {
    return cvv;
  }

  public void setCvv(String cvv) {
    this.cvv = cvv;
  }

  @AssertTrue(message = "expiry date must not be in the past")
  private boolean isExpiryInTheFuture() {
    if (expiryMonth == null || expiryYear == null) {
      return true; // The @NotNull annotation should report this instead.
    }
    return !YearMonth.of(expiryYear, expiryMonth).isBefore(YearMonth.now());
  }

  @Override
  public String toString() {
    return "PaymentRequest{" +
        "cardNumber=" + cardNumber +
        ", expiryMonth=" + expiryMonth +
        ", expiryYear=" + expiryYear +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        '}';
  }
}
