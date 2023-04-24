package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    public void calculateFareCar() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(LocalDateTime.now().minusHours(1));
        ticket.setOutTime(LocalDateTime.now());
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);

        double expectedFare = Fare.CAR_RATE_PER_HOUR * (1 - Fare.FREETIME.toMinutes() / 60.0);
        assertEquals(expectedFare, ticket.getPrice());
    }

    @Test
    public void calculateFareBike() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(LocalDateTime.now().minusHours(1));
        ticket.setOutTime(LocalDateTime.now());
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);

        double expectedFare = Fare.BIKE_RATE_PER_HOUR * (1 - Fare.FREETIME.toMinutes() / 60.0);
        assertEquals(expectedFare, ticket.getPrice());
    }

    @Test
    public void calculateFareUnkownType() {
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);

        ticket.setInTime(LocalDateTime.now().minusHours(1));
        ticket.setOutTime(LocalDateTime.now());
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithFutureInTime() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(LocalDateTime.now().plusHours(1));
        ticket.setOutTime(LocalDateTime.now());
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(LocalDateTime.now().minusMinutes(45));
        ticket.setOutTime(LocalDateTime.now());
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);

        double expectedFare = Fare.BIKE_RATE_PER_HOUR * (0.75 - Fare.FREETIME.toMinutes() / 60.0);
        assertEquals(expectedFare, ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(LocalDateTime.now().minusMinutes(45));
        ticket.setOutTime(LocalDateTime.now());
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);

        double expectedFare = Fare.CAR_RATE_PER_HOUR * (0.75 - Fare.FREETIME.toMinutes() / 60.0);
        assertEquals(expectedFare , ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(LocalDateTime.now().minusDays(1));
        ticket.setOutTime(LocalDateTime.now());
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        
        double expectedFare = Fare.CAR_RATE_PER_HOUR * (24.0 - Fare.FREETIME.toMinutes() / 60.0);
        assertEquals(expectedFare, ticket.getPrice());
    }

}
