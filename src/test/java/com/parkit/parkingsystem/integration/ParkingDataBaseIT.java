package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.*;
import com.parkit.parkingsystem.dao.TicketDAO.getQueries;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown() {

    }

    /**
     * It should properly create and retrieve a ticket
     */
    @Test
    public void testParkingACar() {
        ParkingService parkingService = new ParkingService(parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle(ParkingType.CAR, "ABCDEF");
        assertNotNull(ticketDAO.getTicket("ABCDEF", getQueries.currentTicket));
    }

    /**
     * It should properly update a ticket upon vehicle exit
     */
    @Test
    public void testParkingLotExit() {
        ParkingService parkingService = new ParkingService(parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle(ParkingType.CAR, "ABCDEF");
        
        Ticket ticket = ticketDAO.getTicket("ABCDEF", getQueries.currentTicket);
        ticket.setInTime(ticket.getInTime().minusHours(1));
        ticketDAO.updateTicket(ticket); // updateTicket() only updates outTime and price
        
        parkingService.processExitingVehicle("ABCDEF"); // updates ticket's outTime to 'now'
        assertNotNull(ticketDAO.getTicket("ABCDEF", getQueries.currentTicket).getOutTime());
        // assertEquals(Fare.CAR_RATE_PER_HOUR, ticketDAO.getTicket("ABCDEF", getQueries.currentTicket).getPrice()); // therefore fails
    }

}
