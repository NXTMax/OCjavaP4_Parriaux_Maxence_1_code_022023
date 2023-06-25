package com.parkit.parkingsystem.service;

import java.time.Duration;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.dao.TicketDAO.getQueries;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    private TicketDAO ticketDAO;

    public FareCalculatorService(TicketDAO ticketDAO) {
        this.ticketDAO = ticketDAO;
    }

    /**
     * Calculates and updates the ticket's fare
     * @param ticket The ticket the user wants to end and pay for
     */
    public void calculateFare(Ticket ticket) {
        if((ticket.getOutTime() == null) || (ticket.getOutTime().isBefore(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        Duration ticketDuration = Duration.between(ticket.getInTime(), ticket.getOutTime());
        Duration paidDuration = ticketDuration.minus(Fare.FREETIME).isNegative() ? Duration.ZERO : ticketDuration.minus(Fare.FREETIME);

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(paidDuration.toMinutes() * (1.0 - getUserDiscount(ticket)) * Fare.CAR_RATE_PER_HOUR / 60);
                break;
            }
            case BIKE: {
                ticket.setPrice(paidDuration.toMinutes() * (1.0 - getUserDiscount(ticket)) * Fare.BIKE_RATE_PER_HOUR / 60);
                break;
            }
            default: throw new IllegalArgumentException("Unknown Parking Type");
        }
    }

    /**
     * 
     * @param ticket The ticket the user wants to end and pay for
     * @return The discount the user is eligible to
     */
    private double getUserDiscount(Ticket ticket) {
        String regNum = ticket.getVehicleRegNumber();
        Ticket recentTicket = ticketDAO.getTicket(regNum, getQueries.lastRecentTicket);
        boolean isUserRegular = recentTicket != null;

        return isUserRegular ? Fare.REGULAR_USER_DISCOUNT : 0.0;
    }
}