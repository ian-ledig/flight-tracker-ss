package com.flighttracker.flight_tracker_ss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AdsbdbResponse {

    private Response response;

    @Data
    public static class Response {
        @JsonProperty("flightroute")
        private FlightRoute flightRoute;
    }

    @Data
    public static class FlightRoute {
        private String callsign;
        @JsonProperty("callsign_icao")
        private String callsignIcao;
        @JsonProperty("callsign_iata")
        private String callsignIata;
        private Airline airline;
        private Airport origin;
        private Airport destination;
    }

    @Data
    public static class Airline {
        private String name;
        private String icao;
        private String iata;
        private String country;
        @JsonProperty("country_iso")
        private String countryIso;
        private String callsign;
    }

    @Data
    public static class Airport {
        private String name;
        @JsonProperty("icao_code")
        private String icaoCode;
        @JsonProperty("iata_code")
        private String iataCode;
        private Double latitude;
        private Double longitude;
        private Integer elevation;
        private String municipality;
        @JsonProperty("country_name")
        private String countryName;
        @JsonProperty("country_iso_name")
        private String countryIsoName;
    }
}
