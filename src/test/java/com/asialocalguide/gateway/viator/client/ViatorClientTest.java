package com.asialocalguide.gateway.viator.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.asialocalguide.gateway.viator.dto.*;
import com.asialocalguide.gateway.viator.exception.ViatorApiException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

public class ViatorClientTest {

  private RestClient restClient;
  private MockRestServiceServer mockServer;
  private ViatorClient viatorClient;

  @BeforeEach
  void setup() {
    mockServer = MockRestServiceServer.bindTo(RestClient.builder()).build();
    restClient = RestClient.builder().baseUrl("https://api.sandbox.viator.com/partner").build();
    viatorClient = new ViatorClient(restClient);
  }

  // Helper method to serialize objects to JSON (simplified for testing)
  private String asJsonString(Object obj) {
    try {
      return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // ----------------------------------------------------------
  // Tests for getAllDestinationsForLanguage
  // ----------------------------------------------------------

  @Test
  void getAllDestinationsForLanguage_Success_ReturnsDestinations() {
    // Mock response
    ViatorDestinationResponseDTO mockResponse =
        new ViatorDestinationResponseDTO(
            List.of(new ViatorDestinationDTO(1L, "Paris", "CITY", List.of(100L), null)), 1);
    mockServer
        .expect(ExpectedCount.once(), requestTo("/destinations"))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header("Accept-Language", "en"))
        .andRespond(withSuccess(asJsonString(mockResponse), MediaType.APPLICATION_JSON));

    // Execute
    List<ViatorDestinationDTO> result = viatorClient.getAllDestinationsForLanguage("en");

    // Verify
    assertThat(result).hasSize(1);
    mockServer.verify();
  }

  @Test
  void getAllDestinationsForLanguage_EmptyBody_ReturnsEmptyList() {
    mockServer.expect(requestTo("/destinations")).andRespond(withSuccess("null", MediaType.APPLICATION_JSON));

    List<ViatorDestinationDTO> result = viatorClient.getAllDestinationsForLanguage("en");
    assertThat(result).isEmpty();
    mockServer.verify();
  }

  @Test
  void getAllDestinationsForLanguage_404_ReturnsEmptyList() {
    mockServer
        .expect(requestTo("/destinations"))
        .andExpect(header("Accept-Language", "en")) // Verify language header
        .andRespond(withStatus(HttpStatus.NOT_FOUND));

    List<ViatorDestinationDTO> result = viatorClient.getAllDestinationsForLanguage("en");
    assertThat(result).isEmpty();
    mockServer.verify();
  }

  @Test
  void getAllDestinationsForLanguage_400_ThrowsExceptionWithBodyDetails() {
    String errorBody = "{\"error\": \"Invalid language\"}";
    mockServer
        .expect(requestTo("/destinations"))
        .andRespond(withStatus(HttpStatus.BAD_REQUEST).body(errorBody).contentType(MediaType.APPLICATION_JSON));

    ViatorApiException exception =
        assertThrows(ViatorApiException.class, () -> viatorClient.getAllDestinationsForLanguage("en"));
    assertThat(exception.getMessage()).contains("400").contains(errorBody);
    mockServer.verify();
  }

  @Test
  void getAllDestinationsForLanguage_500_ThrowsException() {
    mockServer.expect(requestTo("/destinations")).andRespond(withServerError());

    assertThrows(ViatorApiException.class, () -> viatorClient.getAllDestinationsForLanguage("en"));
    mockServer.verify();
  }

  // ----------------------------------------------------------
  // Tests for getActivitiesByRequestAndLanguage
  // ----------------------------------------------------------

  @Test
  void getActivitiesByRequestAndLanguage_Success_ReturnsActivities() {
    ViatorActivityResponseDTO mockResponse =
        new ViatorActivityResponseDTO(
            List.of(
                new ViatorActivityDTO(
                    "TOUR-1",
                    "Eiffel Tower Tour",
                    "A great tour",
                    List.of(),
                    new ViatorActivityDTO.ReviewsDTO(List.of(), 100, 4.8),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null)),
            1);
    mockServer
        .expect(requestTo("/products/search"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(content().json(asJsonString(new ViatorActivitySearchDTO(null, null, null, "USD"))))
        .andRespond(withSuccess(asJsonString(mockResponse), MediaType.APPLICATION_JSON));

    List<ViatorActivityDTO> result =
        viatorClient.getActivitiesByRequestAndLanguage("en", new ViatorActivitySearchDTO(null, null, null, "USD"));
    assertThat(result).hasSize(1);
    mockServer.verify();
  }

  @Test
  void getActivitiesByRequestAndLanguage_404_ReturnsEmptyList() {
    mockServer.expect(requestTo("/products/search")).andRespond(withStatus(HttpStatus.NOT_FOUND));

    List<ViatorActivityDTO> result =
        viatorClient.getActivitiesByRequestAndLanguage("en", new ViatorActivitySearchDTO(null, null, null, "USD"));
    assertThat(result).isEmpty();
    mockServer.verify();
  }

  // ----------------------------------------------------------
  // Tests for getAvailabilityByProductCode
  // ----------------------------------------------------------

  @Test
  void getAvailabilityByProductCode_Success_ReturnsAvailability() {
    ViatorActivityAvailabilityDTO mockResponse =
        new ViatorActivityAvailabilityDTO(
            "TOUR-1",
            List.of(
                new ViatorActivityAvailabilityDTO.BookableItem(
                    "OPTION-1",
                    List.of(
                        new ViatorActivityAvailabilityDTO.Season(
                            "2023-10-01",
                            "2023-10-31",
                            List.of(
                                new ViatorActivityAvailabilityDTO.PricingRecord(
                                    List.of("MONDAY"),
                                    List.of(new ViatorActivityAvailabilityDTO.TimedEntry("09:00", List.of())))))))),
            "USD",
            new ViatorActivityAvailabilityDTO.Summary(99.99));
    mockServer
        .expect(requestTo("/availability/schedules/TOUR-1"))
        .andRespond(withSuccess(asJsonString(mockResponse), MediaType.APPLICATION_JSON));

    Optional<ViatorActivityAvailabilityDTO> result = viatorClient.getAvailabilityByProductCode("TOUR-1");
    assertThat(result).isPresent();
    mockServer.verify();
  }

  @Test
  void getAvailabilityByProductCode_404_ReturnsEmptyOptional() {
    mockServer.expect(requestTo("/availability/schedules/INVALID")).andRespond(withStatus(HttpStatus.NOT_FOUND));

    Optional<ViatorActivityAvailabilityDTO> result = viatorClient.getAvailabilityByProductCode("INVALID");
    assertThat(result).isEmpty();
    mockServer.verify();
  }

  @Test
  void getAvailabilityByProductCode_EncodedProductCode_UriEncodedCorrectly() {
    String productCode = "TOUR/123";
    String encodedProductCode = "TOUR%2F123";
    mockServer.expect(requestTo("/availability/schedules/" + encodedProductCode)).andRespond(withSuccess());

    viatorClient.getAvailabilityByProductCode(productCode);
    mockServer.verify();
  }

  // ----------------------------------------------------------
  // Edge Cases and Error Handling
  // ----------------------------------------------------------

  @Test
  void handleViatorError_IncludesStatusCodeAndBodyInException() {
    String errorBody = "Custom error message";
    mockServer
        .expect(requestTo("/destinations"))
        .andRespond(withStatus(HttpStatus.FORBIDDEN).body(errorBody).contentType(MediaType.TEXT_PLAIN));

    ViatorApiException exception =
        assertThrows(ViatorApiException.class, () -> viatorClient.getAllDestinationsForLanguage("en"));
    assertThat(exception.getMessage()).contains("403").contains(errorBody);
    mockServer.verify();
  }

  @Test
  void unexpectedException_WrappedInViatorApiException() {
    // Simulate a network error (e.g., connection refused)
    mockServer
        .expect(requestTo("/destinations"))
        .andRespond(
            request -> {
              throw new IOException("Connection failed");
            });

    ViatorApiException exception =
        assertThrows(ViatorApiException.class, () -> viatorClient.getAllDestinationsForLanguage("en"));
    assertThat(exception.getCause()).hasMessageContaining("Connection failed");
    mockServer.verify();
  }
}
