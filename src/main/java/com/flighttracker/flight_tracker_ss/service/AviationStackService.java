package com.flighttracker.flight_tracker_ss.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flighttracker.flight_tracker_ss.constant.ResponseMessage;
import com.flighttracker.flight_tracker_ss.dto.AviationStackResponse;
import com.flighttracker.flight_tracker_ss.exception.FlightNotFoundException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AviationStackService {

    private static final Logger logger = LoggerFactory.getLogger(AviationStackService.class);
    private final Cache<String, AviationStackResponse> flightCache;
    private final WebClient aviationStackWebClient;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    private AviationStackResponse mockResponse;

    @Value("${aviationstack.access-key}")
    private String accessKey;

    @Value("${aviationstack.mock-enabled:false}")
    private boolean mockEnabled;

    public AviationStackService(WebClient webClient, ObjectMapper objectMapper, ResourceLoader resourceLoader) {
        this.aviationStackWebClient = webClient;
        this.objectMapper = objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.resourceLoader = resourceLoader;
        this.flightCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(100)
                .build();
    }

    @PostConstruct
    public void initMockData() {
        if (mockEnabled) {
            try {
                Resource resource = resourceLoader.getResource("classpath:mock-flights.json");
                if (!resource.exists()) {
                    logger.error("Mock file mock-flights.json not found in classpath");
                    this.mockResponse = null;
                    return;
                }
                logger.debug("Loading mock-flights.json from: {}", resource.getURI());
                this.mockResponse = objectMapper.readValue(resource.getInputStream(), AviationStackResponse.class);
                logger.info("Loaded mock response from mock-flights.json with {} flights",
                        mockResponse != null && mockResponse.getData() != null ? mockResponse.getData().size() : 0);
            } catch (IOException e) {
                logger.error("Failed to load mock-flights.json: {}", e.getMessage(), e);
                this.mockResponse = null;
            }
        } else {
            this.mockResponse = null;
        }
    }

    public Mono<AviationStackResponse> getFlights(String airlineIata, String flightNumber, boolean notDeparted) {
        String flightCode = airlineIata + flightNumber;
        AviationStackResponse cached = flightCache.getIfPresent(flightCode);

        if (mockEnabled) {
            logger.info("Using mocked response for input: {}", flightCode);
            return getMockedResponse(airlineIata, flightNumber, notDeparted);
        }
        else if (cached != null) {
            logger.info("Returning cached flight data for input: {}", flightCode);
            return Mono.just(cached);
        }

        String param = flightNumber.isEmpty() ? "airline_iata" : "flight_iata";
        return aviationStackWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/flights")
                        .queryParam("access_key", accessKey)
                        .queryParam(param, flightCode)
                        .build())
                .retrieve()
                .bodyToMono(AviationStackResponse.class)
                .flatMap(response -> {
                    if (response.getData() == null || response.getData().isEmpty()) {
                        logger.warn(ResponseMessage.FLIGHTS_NOT_FOUND.getMessage(flightCode));
                        return Mono.error(new FlightNotFoundException(flightCode));
                    }

                    flightCache.put(flightCode, response);

                    // filter not departed
                    if(notDeparted){
                        List<AviationStackResponse.Flight> notDepartedFlights = response.getData().stream()
                                .filter(flight -> OffsetDateTime.parse(
                                        flight.getDeparture().getScheduled(),
                                        DateTimeFormatter.ISO_OFFSET_DATE_TIME
                                ).isAfter(OffsetDateTime.now()))
                                .toList();

                        if (notDepartedFlights.isEmpty()) {
                            logger.warn(ResponseMessage.FLIGHTS_NOT_FOUND.getMessage(flightCode));
                            return Mono.error(new FlightNotFoundException(flightCode));
                        }

                        response.setData(notDepartedFlights);
                    }
                    return Mono.just(response);
                })
                .onErrorResume(e -> {
                    logger.error("Error fetching AviationStack data for input {}: {}", flightCode, e.getMessage());
                    return Mono.error(new FlightNotFoundException(flightCode));
                });
    }

    private Mono<AviationStackResponse> getMockedResponse(String airlineIata, String flightNumber, boolean notDeparted) {
        String flightCode = airlineIata + flightNumber;
        if (mockResponse == null || mockResponse.getData() == null) {
            logger.warn("No mock response available for input: {}", flightCode);
            return Mono.error(new FlightNotFoundException(flightCode));
        }

        AviationStackResponse response = new AviationStackResponse();
        response.setPagination(mockResponse.getPagination());

        // searching by airline iata or flight code
        List<AviationStackResponse.Flight> matchingFlights;
        if (flightNumber.isEmpty()) {
            matchingFlights = mockResponse.getData().stream()
                    .filter(flight -> flight.getAirline() != null && airlineIata.equalsIgnoreCase(flight.getAirline().getIata()))
                    .toList();
        } else {
            matchingFlights = mockResponse.getData().stream()
                    .filter(flight -> flight.getFlight() != null && flightCode.equalsIgnoreCase(flight.getFlight().getIata()))
                    .toList();
        }

        response.setData(matchingFlights);

        if (matchingFlights.isEmpty()) {
            logger.warn(ResponseMessage.FLIGHTS_NOT_FOUND.getMessage(flightCode));
            return Mono.error(new FlightNotFoundException(flightCode));
        }

        // filter not departed
        if(notDeparted){
            List<AviationStackResponse.Flight> notDepartedFlights = response.getData().stream()
                    .filter(flight -> OffsetDateTime.parse(
                            flight.getDeparture().getScheduled(),
                            DateTimeFormatter.ISO_OFFSET_DATE_TIME
                    ).isAfter(OffsetDateTime.now()))
                    .toList();

            if (notDepartedFlights.isEmpty()) {
                logger.warn(ResponseMessage.FLIGHTS_NOT_FOUND.getMessage(flightCode));
                return Mono.error(new FlightNotFoundException(flightCode));
            }

            response.setData(notDepartedFlights);
        }

        logger.debug("Mocked response for input {}: {} flights found", flightCode, matchingFlights.size());

        AviationStackResponse.Pagination pagination = new AviationStackResponse.Pagination();
        pagination.setLimit(response.getPagination().getLimit());
        pagination.setOffset(response.getPagination().getOffset());
        pagination.setCount(matchingFlights.size());
        pagination.setTotal(matchingFlights.size());
        response.setPagination(pagination);

        flightCache.put(flightCode, response);
        return Mono.just(response);
    }
}
