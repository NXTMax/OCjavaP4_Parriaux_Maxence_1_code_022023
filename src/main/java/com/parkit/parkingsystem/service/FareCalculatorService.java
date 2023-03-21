package com.parkit.parkingsystem.service;

import java.time.Duration;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    private TicketDAO ticketDAO;

    public FareCalculatorService(TicketDAO ticketDAO) {
        this.ticketDAO = ticketDAO;
    }

    public void calculateFare(Ticket ticket) {
        if((ticket.getOutTime() == null) || (ticket.getOutTime().isBefore(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        Duration ticketDuration = Duration.between(ticket.getInTime(), ticket.getOutTime());

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(ticketDuration.toMinutes() * (1.0 - getUserDiscount(ticket)) * Fare.CAR_RATE_PER_HOUR / 60);
                break;
            }
            case BIKE: {
                ticket.setPrice(ticketDuration.toMinutes() * (1.0 - getUserDiscount(ticket)) * Fare.BIKE_RATE_PER_HOUR / 60);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }

    private double getUserDiscount(Ticket ticket) {

    }
}