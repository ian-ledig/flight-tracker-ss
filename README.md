# Flight Tracking Backend

Spring Boot backend application designed to fetch and serve real-time flight data to a web client. The application provides RESTful endpoints to retrieve flight states, aircraft positions, and other aviation-related data, leveraging reactive programming for efficient handling of real-time data.

## Table of Contents

- Features
- Tech Stack
- Prerequisites
- Installation
- Configuration
- Running the Application
- API Endpoints
- Project Structure
- Contributing
- License

## Features

- Fetch real-time flight data (e.g., callsign, position, altitude, velocity) from the OpenSky Network API.
- Expose RESTful endpoints for a web client to consume flight data.
- Non-blocking HTTP requests using Spring WebFlux (`WebClient`) for efficient and scalable handling of real-time flight data.
- Automatic mapping of JSON responses to Java DTOs using Jackson for seamless data processing.
- Support for streaming updates (e.g., via Server-Sent Events) to enable real-time flight tracking.
- CORS enabled to allow integration with web clients.
- Configurable for authenticated OpenSky API access to bypass rate limits.

## Tech Stack

- **Java**: 21 (LTS)
- **Spring Boot**: 3.3.x
- **Spring WebFlux**: For reactive, non-blocking HTTP requests to handle real-time data efficiently
- **Jackson**: For JSON serialization/deserialization to map OpenSky API responses to DTOs
- **Maven**: Dependency management
- **OpenSky Network API**: Source of real-time flight data (ADS-B)

## Prerequisites

- **Java 21**: Install OpenJDK 21 (e.g., via Adoptium).
- **Maven**: Version 3.8+ for building the project.
- **Git**: To clone the repository.
- **OpenSky Network Account**: Optional, for authenticated API calls (higher rate limits). Register at [OpenSky Network](https://opensky-network.org).
- **Web Client**: A frontend application (e.g., running on `http://localhost:3000`) to consume the API.

## Installation

1. **Clone the repository**:

   ```bash
   git clone https://github.com/ian-ledig/flight-tracker-ss.git
   cd flight-tracker-ss
   ```

2. **Install dependencies**:

   ```bash
   mvn clean install
   ```

## Configuration

1. **Application Properties**:

   Edit `src/main/resources/application.yml` to configure the OpenSky API base URL and optional credentials:

   ```yaml
   opensky:
     api:
       url: https://opensky-network.org/api
       username: your-username # Optional, for authenticated requests
       password: your-password # Optional, for authenticated requests
   server:
     port: 8080
   spring:
     webflux:
       base-path: /api
   ```

2. **CORS Configuration**:

   CORS is enabled for `http://localhost:3000` (default web client development server). Update the allowed origins in `src/main/java/com/yourpackage/config/WebConfig.java` if your client runs on a different host/port.

## Running the Application

1. **Build and run**:

   ```bash
   mvn spring-boot:run
   ```

   The application will start on `http://localhost:8080`.

2. **Verify the API**:

   Use a browser, Postman, or curl to test the endpoint:

   ```bash
   curl http://localhost:8080/api/flights
   ```

## API Endpoints

- **GET /api/flights**  
  Retrieves all flight states from the OpenSky Network API.  
  **Response**: JSON array of flight data (e.g., callsign, position, altitude, velocity).  
  **Example**:

  ```bash
  curl http://localhost:8080/api/flights
  ```

- **GET /api/flights/stream** *(Planned)*  
  Streams real-time flight updates using Server-Sent Events (SSE).  
  **Response**: Continuous stream of flight data updates.  
  **Example**:

  ```bash
  curl http://localhost:8080/api/flights/stream
  ```

*Note*: Additional endpoints (e.g., filtering by region or flight number) can be implemented based on requirements.

## Project Structure

```
flight-tracking-backend/
├── src/
│   ├── main/
│   │   ├── java/com/flighttracker/flight_tracker_ss/
│   │   │   ├── config/         # Configuration classes (WebClient, CORS)
│   │   │   ├── controller/     # REST controllers for API endpoints
│   │   │   ├── service/        # Business logic for fetching/processing flight data
│   │   │   ├── dto/           # Data Transfer Objects for mapping JSON responses
│   │   │   └── Application.java # Main Spring Boot application class
│   │   └── resources/
│   │       └── application.yml # Configuration file
├── pom.xml                     # Maven dependencies
└── README.md                   # This file
```

## Contributing

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -m "Add your feature"`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.