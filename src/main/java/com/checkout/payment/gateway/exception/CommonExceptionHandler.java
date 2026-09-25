package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.model.ErrorResponse;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  @ExceptionHandler(EventProcessingException.class)
  public ResponseEntity<ErrorResponse> handleException(EventProcessingException ex) {
    LOG.error("Exception happened", ex);
    return new ResponseEntity<>(new ErrorResponse("Error processing event"),
        HttpStatus.NOT_FOUND);
  }
  
  @ExceptionHandler(PaymentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleException(PaymentNotFoundException ex) {
    LOG.error("Exception happened", ex);
    return new ResponseEntity<>(new ErrorResponse("Payment not found"),
        HttpStatus.NOT_FOUND);
  }
  
  @ExceptionHandler(BankServiceException.class)
  public ResponseEntity<ErrorResponse> handleException(BankServiceException ex) {
    LOG.error("Exception happened", ex);
    return new ResponseEntity<>(new ErrorResponse("Payment not processed: bank service unavailable"),
        HttpStatus.SERVICE_UNAVAILABLE);
  }
  
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleException(MethodArgumentNotValidException ex) {
    List<String> errors = ex.getBindingResult().getFieldErrors().stream()
      .map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList();
    LOG.warn("Rejected payment request: {}", errors);
    return new ResponseEntity<>(new ErrorResponse("Payment rejected: invalid request", errors),
        HttpStatus.BAD_REQUEST);
  }
}
