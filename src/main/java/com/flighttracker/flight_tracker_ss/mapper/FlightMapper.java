package com.flighttracker.flight_tracker_ss.mapper;

import com.flighttracker.flight_tracker_ss.dto.AviationStackResponse;
import com.flighttracker.flight_tracker_ss.dto.FlightsDTO;
import org.mapstruct.*;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

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

    @AfterMapping
    default void calculateDuration(@MappingTarget FlightsDTO dto, AviationStackResponse.Flight flight) {
        String departureTime = flight.getDeparture().getEstimated() != null
                ? flight.getDeparture().getEstimated()
                : flight.getDeparture().getScheduled();
        String arrivalTime = flight.getArrival().getEstimated() != null
                ? flight.getArrival().getEstimated()
                : flight.getArrival().getScheduled();

        try {
            OffsetDateTime departure = OffsetDateTime.parse(departureTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            OffsetDateTime arrival = OffsetDateTime.parse(arrivalTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            Duration duration = Duration.between(departure, arrival);
            dto.setDuration(duration.toMinutes());
        } catch (Exception e) {
            dto.setDuration(0);
        }
    }
}