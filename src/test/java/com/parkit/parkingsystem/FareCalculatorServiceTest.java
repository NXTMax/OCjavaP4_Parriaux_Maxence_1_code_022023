package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.dao.TicketDAO.getQueries;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class FareCalculatorServiceTest {

    private Ticket ticket;

    @Mock
    private static TicketDAO ticketDAO;
    @InjectMocks
    private static FareCalculatorService fareCalculatorService;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService(ticketDAO);
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    /**
     * It should properly calculate fare for a 1h car ticket
     */
    @Test
    public void calculateFareCar() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(LocalDateTime.now().minusHours(1));
        ticket.setOutTime(LocalDateTime.now());
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
    }

    /**
     * It should properly calculate fare for a 1h bike ticket
     */
    @Test
    public void calculateFareBike() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(LocalDateTime.now().minusHours(1));
        ticket.setOutTime(LocalDateTime.now());
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
    }

    /**
     * It should throw an exception when vehicle/parking type is not defined
     */
    @Test
    public void calculateFareUnknownType() {
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);

        ticket.setInTime(LocalDateTime.now().minusHours(1));
        ticket.setOutTime(LocalDateTime.now());
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    /**
     * It should throw an exception when InTime is after OutTime
     */
    @Test
    public void calculateFareBikeWithFutureInTime() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(LocalDateTime.now().plusHours(1));
        ticket.setOutTime(LocalDateTime.now());
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    /**
     * It should properly calculate fare for a bike ticket lasting less than a hour
     */
    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(LocalDateTime.now().minusMinutes(45));
        ticket.setOutTime(LocalDateTime.now());
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
    }

    /**
     * It should properly calculate fare for a car ticket lasting less than a hour
     */
    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(LocalDateTime.now().minusMinutes(45));
        ticket.setOutTime(LocalDateTime.now());
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals((0.75 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    /**
     * It should properly calculate fare for a ticket spanning several days
     */
    @Test
    public void calculateFareCarWithMoreThanADayParkingTime() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(LocalDateTime.now().minusDays(1));
        ticket.setOutTime(LocalDateTime.now());
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals((24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
    }

    /**
     * It should allow discount to a user who've come less that 15 days ago
     */
    @Test
    public void calculateFareForRegularUser() {
        String testCarRegNum = "AB-123-CD";
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        
        Ticket recentTicket = new Ticket();
        recentTicket.setVehicleRegNumber(testCarRegNum);
        recentTicket.setInTime(LocalDateTime.now().minusDays(3).minusHours(1));
        recentTicket.setOutTime(LocalDateTime.now().minusDays(3));
        recentTicket.setParkingSpot(parkingSpot);

        when(ticketDAO.getTicket(testCarRegNum, getQueries.lastRecentTicket)).thenReturn(recentTicket);

        ticket.setVehicleRegNumber(testCarRegNum);
        ticket.setInTime(LocalDateTime.now().minusHours(1));
        ticket.setOutTime(LocalDateTime.now());
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);
        double expectedFare = 60 * Fare.CAR_RATE_PER_HOUR * (1 - Fare.REGULAR_USER_DISCOUNT) / 60;
        assertEquals(expectedFare, ticket.getPrice());
    }
}
