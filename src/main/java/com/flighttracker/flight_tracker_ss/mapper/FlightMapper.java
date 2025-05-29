package com.flighttracker.flight_tracker_ss.mapper;

import com.flighttracker.flight_tracker_ss.dto.AviationStackResponse;
import com.flighttracker.flight_tracker_ss.dto.FlightsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface FlightMapper {

    @Mapping(target = "airlineIata", source = "airline.iata")
    @Mapping(target = "flightNumber", source = "flight.number")
    @Mapping(target = "depIata", source = "departure.iata")
    @Mapping(target = "arrIata", source = "arrival.iata")
    @Mapping(target = "depScheduled", source = "departure.scheduled")
    @Mapping(target = "arrScheduled", source = "arrival.scheduled")
    @Mapping(target = "depEstimated", source = "departure.estimated")
    @Mapping(target = "arrEstimated", source = "arrival.estimated")
    @Mapping(target = "aircraftIata", source = "aircraft.iata")
    FlightsDTO toFlightsDTO(AviationStackResponse.Flight flight);
}