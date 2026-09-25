package com.checkout.payment.gateway.integration;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@SuppressWarnings("null") // Null type safety checks already actioned in the respective classes.
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class PaymentGatewayIntegrationTest {

  @SuppressWarnings("resource") // Test setup, so resource leaks shouldn't occur during runtime.
  @Container
  static GenericContainer<?> bankSimulator =
      new GenericContainer<>(DockerImageName.parse("bbyars/mountebank:2.8.1"))
          .withExposedPorts(8080)
          .withCommand("--configfile", "/imposters/bank_simulator.ejs", "--allowInjection")
          .withCopyFileToContainer(
              MountableFile.forHostPath("imposters/bank_simulator.ejs"),
              "/imposters/bank_simulator.ejs")
          .waitingFor(Wait.forHttp("/payments").forStatusCode(400));

  @DynamicPropertySource
  static void bankSimulatorProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "bank.simulator.url",
        () ->
            String.format(
                "http://%s:%d/payments",
                bankSimulator.getHost(), bankSimulator.getMappedPort(8080)));
  }

  @Autowired private MockMvc mvc;

  @Test
  void authorizeSuccessfulWhenCardNumberEndsInOddDigit() throws Exception {
    mvc.perform(
            MockMvcRequestBuilders.post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPaymentRequestJson("01234567898765", "GBP")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.cardNumberLastFour").value("8765"))
        .andExpect(header().string("Location", Matchers.startsWith("/payments")));
  }

  @Test
  void authorizeDeclinedWhenCardNumberEndsInEvenDigit() throws Exception {
    mvc.perform(
            MockMvcRequestBuilders.post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPaymentRequestJson("012345678987654", "GBP")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("Declined"))
        .andExpect(jsonPath("$.cardNumberLastFour").value("7654"));
  }

  @Test
  void authorizeUnavailableWhenCardNumberEndsWithZero() throws Exception {
    mvc.perform(
            MockMvcRequestBuilders.post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPaymentRequestJson("012345678987650", "GBP")))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void authorizeRejectedWhenCurrencyNotExcepted() throws Exception {
    mvc.perform(
            MockMvcRequestBuilders.post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPaymentRequestJson("01234567898765", "XXX")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void paymentCanBeRetrievedAfterBeingAuthorized() throws Exception {
    String responseJson =
        mvc.perform(
                MockMvcRequestBuilders.post("/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validPaymentRequestJson("01234567898765", "GBP")))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    String id = JsonPath.read(responseJson, "$.id");

    mvc.perform(MockMvcRequestBuilders.get("/payments/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.cardNumberLastFour").value("8765"));
  }

  private String validPaymentRequestJson(String cardNumber, String currency) {
    return """
    {
        "card_number": "%s",
        "expiry_month": 12,
        "expiry_year": 2027,
        "currency": "%s",
        "amount": 5525,
        "cvv": "001"
    }
    """
        .formatted(cardNumber, currency);
  }
}
