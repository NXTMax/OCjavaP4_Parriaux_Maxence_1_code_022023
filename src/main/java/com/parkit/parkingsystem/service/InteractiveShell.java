package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.Generated;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.*;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Generated
public class InteractiveShell {

    private static final Logger logger = LogManager.getLogger("InteractiveShell");
    private static InputReaderUtil inputReaderUtil;

    public static void loadInterface() {
        logger.info("App initialized!!!");
        System.out.println("Welcome to Parking System!");

        boolean continueApp = true;
        inputReaderUtil = new InputReaderUtil();
        ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO();
        TicketDAO ticketDAO = new TicketDAO();
        ParkingService parkingService = new ParkingService(parkingSpotDAO, ticketDAO);

        while(continueApp) {
            loadMenu();
            int option = inputReaderUtil.readSelection();
            switch(option) {
                case 1: {
                    Ticket newTicket = parkingService.processIncomingVehicle(getVehicleType(), getVehicleRegNumber());
                    System.out.println("Please park your vehicle in spot number:" + newTicket.getParkingSpot().getId());
                    System.out.println("Recorded in-time for vehicle number:" + newTicket.getVehicleRegNumber() + " is:" + newTicket.getInTime());
                    break;
                }
                case 2: {
                    Ticket terminatedTicket = parkingService.processExitingVehicle(getVehicleRegNumber());
                    if (terminatedTicket == null) {
                        System.out.println("Unable to update ticket information. Error occurred");
                        break;
                    }
                    System.out.println("Please pay the parking fare:" + terminatedTicket.getPrice());
                    System.out.println("Recorded out-time for vehicle number:" + terminatedTicket.getVehicleRegNumber() + " is:" + terminatedTicket.getOutTime());
                    break;
                }
                case 3: {
                    System.out.println("Exiting from the system!");
                    continueApp = false;
                    break;
                }
                default: System.out.println("Unsupported option. Please enter a number corresponding to the provided menu");
            }
        }
    }

    private static void loadMenu() {
        System.out.println("Please select an option. Simply enter the number to choose an action");
        System.out.println("1 New Vehicle Entering - Allocate Parking Space");
        System.out.println("2 Vehicle Exiting - Generate Ticket Price");
        System.out.println("3 Shutdown System");
    }

    private static ParkingType getVehicleType() {
        System.out.println("Please select vehicle type from menu");
        System.out.println("1 CAR");
        System.out.println("2 BIKE");
        int input = inputReaderUtil.readSelection();
        switch(input) {
            case 1: {
                return ParkingType.CAR;
            }
            case 2: {
                return ParkingType.BIKE;
            }
            default: {
                System.out.println("Incorrect input provided");
                throw new IllegalArgumentException("Entered input is invalid");
            }
        }
    }

    private static String getVehicleRegNumber() {
        System.out.println("Please type the vehicle registration number and press enter key");
        while (true) {
            try { return inputReaderUtil.readVehicleRegistrationNumber(); }
            catch (IllegalArgumentException e) { continue; }
            catch (Exception e) { return null; }
        }
    }

}
