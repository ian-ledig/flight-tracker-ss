package com.flighttracker.flight_tracker_ss.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flighttracker.flight_tracker_ss.dto.AviationStackResponse;
import com.flighttracker.flight_tracker_ss.exception.FlightNotFoundException;
import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AviationStackServiceTest {

    @Mock
    private WebClient aviationStackWebClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Resource resource;

    @InjectMocks
    private AviationStackService aviationStackService;

    private final String accessKey = "test-access-key";
    private final String flightCode = "AA123";
    private final String mockJson = "{\"pagination\": {\"limit\": 100, \"offset\": 0, \"count\": 1, \"total\": 1}, " +
            "\"data\": [{\"flight_date\": \"2025-06-05\", \"flight_status\": \"scheduled\", " +
            "\"departure\": {\"airport\": \"JFK\", \"timezone\": \"America/New_York\", \"iata\": \"JFK\", \"icao\": \"KJFK\", " +
            "\"terminal\": \"1\", \"gate\": \"A1\", \"scheduled\": \"2025-06-05T10:00:00+00:00\", \"estimated\": null}, " +
            "\"arrival\": {\"airport\": \"LHR\", \"timezone\": \"Europe/London\", \"iata\": \"LHR\", \"icao\": \"EGLL\", " +
            "\"terminal\": \"2\", \"gate\": \"B2\", \"scheduled\": \"2025-06-05T18:00:00+00:00\", \"estimated\": null}, " +
            "\"airline\": {\"name\": \"American Airlines\", \"iata\": \"AA\", \"icao\": \"AAL\"}, " +
            "\"flight\": {\"number\": \"123\", \"iata\": \"AA123\", \"icao\": \"AAL123\"}, " +
            "\"aircraft\": {\"registration\": \"N123AA\", \"iata\": \"B777\", \"icao\": \"B777\", \"icao24\": \"ABC123\"}}]}";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aviationStackService, "accessKey", accessKey);
        ReflectionTestUtils.setField(aviationStackService, "mockEnabled", false);
    }

    @Test
    void testGetFlightsFromCache() {
        AviationStackResponse cachedResponse = new AviationStackResponse();
        AviationStackResponse.Flight flight = new AviationStackResponse.Flight();
        AviationStackResponse.FlightDetails flightDetails = new AviationStackResponse.FlightDetails();
        flightDetails.setIata("AA123");
        flightDetails.setNumber("123");
        flight.setFlight(flightDetails);
        AviationStackResponse.Airline airline = new AviationStackResponse.Airline();
        airline.setIata("AA");
        flight.setAirline(airline);
        cachedResponse.setData(Collections.singletonList(flight));
        Cache<String, AviationStackResponse> cache = mock(Cache.class);
        ReflectionTestUtils.setField(aviationStackService, "flightCache", cache);
        when(cache.getIfPresent(flightCode)).thenReturn(cachedResponse);

        Mono<AviationStackResponse> result = aviationStackService.getFlights("AA", "123", false);

        StepVerifier.create(result)
                .expectNext(cachedResponse)
                .verifyComplete();
        verifyNoInteractions(aviationStackWebClient);
    }

    @Test
    void testGetFlightsMockEnabled() throws IOException {
        ReflectionTestUtils.setField(aviationStackService, "mockEnabled", true);

        ObjectMapper realObjectMapper = new ObjectMapper();
        realObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ReflectionTestUtils.setField(aviationStackService, "objectMapper", realObjectMapper);

        when(resourceLoader.getResource("classpath:mock-flights.json")).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(mockJson.getBytes()));

        aviationStackService.initMockData();

        Mono<AviationStackResponse> result = aviationStackService.getFlights("AA", "123", false);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getData().size() == 1
                        && response.getData().getFirst().getFlight().getIata().equals("AA123")
                        && response.getData().getFirst().getAirline().getIata().equals("AA")
                        && response.getPagination().getCount() == 1)
                .verifyComplete();
    }

    @Test
    void testGetFlightsLongHaulFilter() throws IOException {
        ObjectMapper realObjectMapper = new ObjectMapper();
        realObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ReflectionTestUtils.setField(aviationStackService, "objectMapper", realObjectMapper);

        AviationStackResponse apiResponse = realObjectMapper.readValue(mockJson, AviationStackResponse.class);

        lenient().when(aviationStackWebClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.bodyToMono(AviationStackResponse.class)).thenReturn(Mono.just(apiResponse));

        Mono<AviationStackResponse> result = aviationStackService.getFlights("AA", "123", true);

        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    AviationStackResponse.Flight flight = response.getData().get(0);
                    return response.getData().size() == 1
                            && flight.getFlight().getIata().equals("AA123")
                            && Duration.between(
                            OffsetDateTime.parse(flight.getDeparture().getScheduled(), DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                            OffsetDateTime.parse(flight.getArrival().getScheduled(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    ).toHours() >= 6;
                })
                .verifyComplete();
    }

    @Test
    void testGetFlightsByAirlineIata() throws IOException {
        ReflectionTestUtils.setField(aviationStackService, "mockEnabled", true);

        ObjectMapper realObjectMapper = new ObjectMapper();
        realObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ReflectionTestUtils.setField(aviationStackService, "objectMapper", realObjectMapper);

        when(resourceLoader.getResource("classpath:mock-flights.json")).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(mockJson.getBytes()));

        aviationStackService.initMockData();

        Mono<AviationStackResponse> result = aviationStackService.getFlights("AA", "", false);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getData().size() == 1
                        && response.getData().getFirst().getAirline().getIata().equals("AA"))
                .verifyComplete();
    }

    @Test
    void testGetFlightsNotFound() throws IOException {
        ObjectMapper realObjectMapper = new ObjectMapper();
        realObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ReflectionTestUtils.setField(aviationStackService, "objectMapper", realObjectMapper);

        AviationStackResponse emptyResponse = new AviationStackResponse();
        emptyResponse.setData(Collections.emptyList());

        lenient().when(aviationStackWebClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.bodyToMono(AviationStackResponse.class)).thenReturn(Mono.just(emptyResponse));

        Mono<AviationStackResponse> result = aviationStackService.getFlights("AA", "123", false);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof FlightNotFoundException
                        && throwable.getMessage().contains("AA123"))
                .verify();
    }

    @Test
    void testGetFlightsLongHaulNoMatch() throws IOException {
        ObjectMapper realObjectMapper = new ObjectMapper();
        realObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ReflectionTestUtils.setField(aviationStackService, "objectMapper", realObjectMapper);

        String shortHaulJson = "{\"pagination\": {\"limit\": 100, \"offset\": 0, \"count\": 1, \"total\": 1}, " +
                "\"data\": [{\"flight_date\": \"2025-06-05\", \"flight_status\": \"scheduled\", " +
                "\"departure\": {\"airport\": \"JFK\", \"timezone\": \"America/New_York\", \"iata\": \"JFK\", \"icao\": \"KJFK\", " +
                "\"scheduled\": \"2025-06-05T10:00:00+00:00\", \"estimated\": null}, " +
                "\"arrival\": {\"airport\": \"BOS\", \"timezone\": \"America/New_York\", \"iata\": \"BOS\", \"icao\": \"KBOS\", " +
                "\"scheduled\": \"2025-06-05T11:00:00+00:00\", \"estimated\": null}, " +
                "\"airline\": {\"name\": \"American Airlines\", \"iata\": \"AA\", \"icao\": \"AAL\"}, " +
                "\"flight\": {\"number\": \"123\", \"iata\": \"AA123\", \"icao\": \"AAL123\"}}]}";
        AviationStackResponse shortHaulResponse = realObjectMapper.readValue(shortHaulJson, AviationStackResponse.class);

        lenient().when(aviationStackWebClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.bodyToMono(AviationStackResponse.class)).thenReturn(Mono.just(shortHaulResponse));

        Mono<AviationStackResponse> result = aviationStackService.getFlights("AA", "123", true);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof FlightNotFoundException
                        && throwable.getMessage().contains("AA123"))
                .verify();
    }

    @Test
    void testInitMockDataFileNotFound() throws IOException {
        ReflectionTestUtils.setField(aviationStackService, "mockEnabled", true);

        ObjectMapper realObjectMapper = new ObjectMapper();
        realObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ReflectionTestUtils.setField(aviationStackService, "objectMapper", realObjectMapper);

        when(resourceLoader.getResource("classpath:mock-flights.json")).thenReturn(resource);
        when(resource.exists()).thenReturn(false);

        aviationStackService.initMockData();

        assert ReflectionTestUtils.getField(aviationStackService, "mockResponse") == null;
    }

    @Test
    void testInitMockDataIOException() throws IOException {
        ReflectionTestUtils.setField(aviationStackService, "mockEnabled", true);

        ObjectMapper realObjectMapper = new ObjectMapper();
        realObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ReflectionTestUtils.setField(aviationStackService, "objectMapper", realObjectMapper);

        when(resourceLoader.getResource("classpath:mock-flights.json")).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenThrow(new IOException("File read error"));

        aviationStackService.initMockData();

        assert ReflectionTestUtils.getField(aviationStackService, "mockResponse") == null;
    }
}
