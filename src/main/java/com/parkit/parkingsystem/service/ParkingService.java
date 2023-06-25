package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.dao.TicketDAO.getQueries;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;

public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private FareCalculatorService fareCalculatorService;

    private ParkingSpotDAO parkingSpotDAO;
    private TicketDAO ticketDAO;

    public ParkingService(ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
        this.fareCalculatorService = new FareCalculatorService(ticketDAO);
    }

    /**
     * Finds an available parking slot and creates a ticket for the incoming vehicle
     * @param parkingType The type of the incoming vehicle
     * (either {@code ParkingType.CAR} or {@code ParkingType.BIKE})
     * @param vehicleRegNumber The license plate of the incoming vehicle
     * @return The created ticket
     */
    public Ticket processIncomingVehicle(ParkingType parkingType, String vehicleRegNumber) {
        Ticket ticket = null;
        try{
            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable(parkingType);
            if(parkingSpot !=null && parkingSpot.getId() > 0) {
                parkingSpot.setAvailable(false);
                parkingSpotDAO.updateParking(parkingSpot); //allot this parking space and mark it's availability as false

                LocalDateTime inTime = LocalDateTime.now();
                ticket = new Ticket();
                //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
                //ticket.setId(ticketID);
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(0);
                ticket.setInTime(inTime);
                ticket.setOutTime(null);
                ticketDAO.saveTicket(ticket);
                logger.trace("Generated Ticket and saved in DB");
            }
        } catch(Exception e) {
            logger.error("Unable to process incoming vehicle",e);
        }
        return ticket;
    }

    /**
     * Calls {@code getNextAvailableSlot(parkingType)} and handles some bound cases and DB errors
     * @param parkingType The type of parking slot to searing for
     * (either {@code ParkingType.CAR} or {@code ParkingType.BIKE})
     * @return The first (smallest ID) parking slot matching the provided parking type
     */
    public ParkingSpot getNextParkingNumberIfAvailable(ParkingType parkingType) {
        int parkingNumber=0;
        ParkingSpot parkingSpot = null;
        try{
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if(parkingNumber > 0) {
                parkingSpot = new ParkingSpot(parkingNumber,parkingType, true);
            } else{
                throw new Exception("Error fetching parking number from DB. Parking slots might be full");
            }
        } catch(Exception e) {
            logger.error("Error fetching next available parking slot", e);
        }
        return parkingSpot;
    }

    /**
     * Of the ticket bound to the given {@code vehicleRegNumber}, updates its OutTime and Fare,
     * then sets the corresponding parking slot as available
     * @param vehicleRegNumber The license plate of the leaving vehicle
     * @return The updated ticket
     */
    public Ticket processExitingVehicle(String vehicleRegNumber) {
        Ticket ticket = null;
        try {
            ticket = ticketDAO.getTicket(vehicleRegNumber, getQueries.currentTicket);
            LocalDateTime outTime = LocalDateTime.now();
            ticket.setOutTime(outTime);
            fareCalculatorService.calculateFare(ticket);
            if(ticketDAO.updateTicket(ticket)) {
                ParkingSpot parkingSpot = ticket.getParkingSpot();
                parkingSpot.setAvailable(true);
                parkingSpotDAO.updateParking(parkingSpot);
            } else{
                ticket = null;
            }
        } catch(Exception e) {
            logger.error("Unable to process exiting vehicle",e);
        }
        return ticket;
    }
}
