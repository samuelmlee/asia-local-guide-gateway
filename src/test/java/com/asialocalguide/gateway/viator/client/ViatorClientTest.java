package com.asialocalguide.gateway.viator.client;

import com.asialocalguide.gateway.viator.dto.*;
import com.asialocalguide.gateway.viator.exception.ViatorApiException;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WireMockTest
public class ViatorClientTest {

  private ViatorClient viatorClient;

  @RegisterExtension
  static WireMockExtension wireMock = WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @BeforeEach
  void setup() {
    RestClient restClient = RestClient.builder().baseUrl(wireMock.baseUrl()).build();

    viatorClient = new ViatorClient(restClient);
  }

  private String asJsonString(Object obj) throws Exception {
    return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
  }

  // ----------------------------------------------------------
  // Tests for getAllDestinationsForLanguage
  // ----------------------------------------------------------

  @Test
  void getAllDestinationsForLanguage_Success_ReturnsDestinations() throws Exception {
    ViatorDestinationResponseDTO mockResponse =
        new ViatorDestinationResponseDTO(
            List.of(
                new ViatorDestinationDTO(1L, "France", "COUNTRY", List.of(20L, 85L, 100L), null),
                new ViatorDestinationDTO(2L, "Paris", "CITY", List.of(1L, 85L, 100L), null)),
            1);

    wireMock.stubFor(
        get(urlPathEqualTo("/destinations"))
            .withHeader("Accept-Language", equalTo("en"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(asJsonString(mockResponse))));

    List<ViatorDestinationDTO> result = viatorClient.getAllDestinationsForLanguage("en");

    assertThat(result).hasSize(2);
    wireMock.verify(1, getRequestedFor(urlPathEqualTo("/destinations")));
  }

  @Test
  void getAllDestinationsForLanguage_404_ReturnsEmptyList() {
    wireMock.stubFor(get(urlPathEqualTo("/destinations")).willReturn(aResponse().withStatus(404)));

    assertThatThrownBy(() -> viatorClient.getAllDestinationsForLanguage("en"), "404")
        .isInstanceOf(ViatorApiException.class);

    wireMock.verify(1, getRequestedFor(urlPathEqualTo("/destinations")));
  }

  @Test
  void getAllDestinationsForLanguage_400_ThrowsExceptionWithBodyDetails() throws Exception {
    String errorBody = "{\"error\": \"Invalid language\"}";

    wireMock.stubFor(
        get(urlPathEqualTo("/destinations"))
            .willReturn(
                aResponse()
                    .withStatus(400)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(errorBody)));

    ViatorApiException exception =
        assertThrows(ViatorApiException.class, () -> viatorClient.getAllDestinationsForLanguage("en"));

    assertThat(exception.getMessage()).contains("400").contains(errorBody);
  }

  // ----------------------------------------------------------
  // Tests for getActivitiesByRequestAndLanguage
  // ----------------------------------------------------------

  @Test
  void getActivitiesByRequestAndLanguage_Success_ReturnsActivities() throws Exception {
    ViatorActivityResponseDTO mockResponse = new ViatorActivityResponseDTO(List.of(createTestActivity()), 1);

    wireMock.stubFor(
        post(urlPathEqualTo("/products/search"))
            .withHeader("Accept-Language", equalTo("en"))
            .withRequestBody(equalToJson(asJsonString(new ViatorActivitySearchDTO(null, null, null, "USD"))))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(asJsonString(mockResponse))));

    List<ViatorActivityDTO> result =
        viatorClient.getActivitiesByRequestAndLanguage("en", new ViatorActivitySearchDTO(null, null, null, "USD"));

    assertThat(result).hasSize(1);
    wireMock.verify(1, postRequestedFor(urlPathEqualTo("/products/search")));
  }

  // ----------------------------------------------------------
  // Tests for getAvailabilityByProductCode
  // ----------------------------------------------------------

  @Test
  void getAvailabilityByProductCode_EncodedProductCode_UriEncodedCorrectly() {
    String productCode = "TOUR/123";

    wireMock.stubFor(get(urlPathEqualTo("/availability/schedules/TOUR%2F123")).willReturn(aResponse().withStatus(200)));

    viatorClient.getAvailabilityByProductCode(productCode);

    wireMock.verify(1, getRequestedFor(urlPathEqualTo("/availability/schedules/TOUR%2F123")));
  }

  // ----------------------------------------------------------
  // Edge Cases and Error Handling
  // ----------------------------------------------------------

  @Test
  void handleViatorError_IncludesStatusCodeAndBodyInException() {
    String errorBody = "Custom error message";

    wireMock.stubFor(get(urlPathEqualTo("/destinations")).willReturn(aResponse().withStatus(403).withBody(errorBody)));

    ViatorApiException exception =
        assertThrows(ViatorApiException.class, () -> viatorClient.getAllDestinationsForLanguage("en"));

    assertThat(exception.getMessage()).contains("403").contains(errorBody);
  }

  @Test
  void unexpectedException_WrappedInViatorApiException() {
    wireMock.stubFor(
        get(urlPathEqualTo("/destinations")).willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

    assertThatThrownBy(() -> viatorClient.getAllDestinationsForLanguage("en")).isInstanceOf(ViatorApiException.class);
  }

  private ViatorActivityDTO createTestActivity() {
    return new ViatorActivityDTO(
        "TOUR-1",
        "Eiffel Tower Tour",
        "A great tour",
        List.of(),
        new ViatorActivityDTO.ReviewsDTO(List.of(), 100, 4.8),
        new ViatorActivityDTO.DurationDTO(null, null, 120),
        "CONFIRMATION_TYPE",
        "ITINERARY_TYPE",
        new ViatorActivityDTO.PricingDTO(new ViatorActivityDTO.PricingDTO.SummaryDTO(99.99, 129.99), "USD"),
        "https://tour-url",
        List.of(),
        List.of(1, 2, 3),
        List.of("FLAG1"),
        new ViatorActivityDTO.TranslationInfoDTO(false, "SOURCE"));
  }
}
